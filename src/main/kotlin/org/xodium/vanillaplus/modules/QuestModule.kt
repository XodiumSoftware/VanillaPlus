@file:OptIn(ExperimentalUuidApi::class)

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import kotlinx.serialization.Serializable
import net.kyori.adventure.title.Title
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.databases.QuestDatabase
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.inventories.QuestInventory
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import org.xodium.vanillaplus.utils.Utils.MM
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

/** Represents a module handling quest mechanics within the system. */
internal object QuestModule : ModuleInterface {
    private val questInventory = QuestInventory()
    private val store by lazy { QuestDatabase() }

    val assignedQuests: MutableMap<Uuid, List<Quest>> = mutableMapOf()
    private val allQuestsRewardClaimed: MutableSet<Uuid> = mutableSetOf()

    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("quests")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .playerExecuted { player, _ -> questInventory.openFor(player) },
                "This command allows you to open the quests interface",
                listOf("q"),
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.quests".lowercase(),
                "Allows use of the quests command",
                PermissionDefault.TRUE,
            ),
        )

    @EventHandler
    fun on(event: InventoryClickEvent) = questInventory.inventoryClick(event)

    @EventHandler
    fun on(event: PlayerJoinEvent) = loadOrAssign(event)

    @EventHandler(ignoreCancelled = true)
    fun on(event: EntityDeathEvent) {
        incrementMatchingQuests(
            player = event.entity.killer ?: return,
            predicate = { it.target.matches(event.entityType) },
            incrementBy = 1,
        )
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: EntityPickupItemEvent) {
        val itemStack = event.item.itemStack

        incrementMatchingQuests(
            player = event.entity as? Player ?: return,
            predicate = { it.target.matches(itemStack.type) },
            incrementBy = itemStack.amount,
        )
    }

    /**
     * Loads or assigns quests to a player upon joining the server.
     * @param event The player join event containing player information.
     */
    private fun loadOrAssign(event: PlayerJoinEvent) {
        val player = event.player
        val id = player.uniqueId.toKotlinUuid()
        val loaded = store.load(id)

        if (loaded.isNotEmpty()) {
            assignedQuests[id] = loaded
        } else {
            assignInitQuests(event)
            store.save(id, assignedQuests[id].orEmpty())
        }

        if (store.hasClaimedAllReward(id)) allQuestsRewardClaimed.add(id) else allQuestsRewardClaimed.remove(id)
    }

    /**
     * Retrieves the list of quests assigned to a player.
     * @param player The player whose quests are to be retrieved.
     * @return List of quests assigned to the player.
     */
    fun getAssignedQuests(player: Player): List<Quest> = assignedQuests[player.uniqueId.toKotlinUuid()].orEmpty()

    /**
     * Checks if a player has claimed the reward for completing all quests.
     * @param player The player to check.
     * @return true if the player has claimed the all-quests reward, false otherwise.
     */
    fun hasClaimedAllQuestsReward(player: Player): Boolean =
        allQuestsRewardClaimed.contains(player.uniqueId.toKotlinUuid())

    /**
     * Increments the progress of quests matching a given predicate for a player.
     * @param player The player whose quests are to be updated.
     * @param predicate A function that determines which quest requirements to increment.
     * @param incrementBy The amount by which to increment the progress.
     */
    private fun incrementMatchingQuests(
        player: Player,
        predicate: (Quest.Requirement) -> Boolean,
        incrementBy: Int,
    ) {
        if (incrementBy <= 0) return

        val id = player.uniqueId.toKotlinUuid()
        val quests = assignedQuests[id] ?: return
        val changed = false

        quests
            .asSequence()
            .filter { !it.requirement.isComplete }
            .filter { predicate(it.requirement) }
            .forEach { quest ->
                val req = quest.requirement
                val before = req.currentProgress
                val after = (before + incrementBy).coerceAtMost(req.targetAmount)

                if (after != before) req.currentProgress = after

                if (req.targetAmount in (before + 1)..after) {
                    giveReward(player, quest.reward)
                    player.showTitle(
                        Title.title(
                            MM.deserialize("<green><b>Quest Completed!</b></green>"),
                            MM.deserialize(
                                "<yellow>Reward: ${quest.reward.description}</yellow>",
                            ),
                        ),
                    )
                }
            }
        if (changed) store.save(id, quests)

        maybeGiveAllQuestsReward(player)
    }

    /**
     * Set of player UUIDs who have claimed the all-quests reward.
     * @param player The player to potentially give the reward to.
     */
    private fun maybeGiveAllQuestsReward(player: Player) {
        val id = player.uniqueId.toKotlinUuid()

        if (allQuestsRewardClaimed.contains(id)) return

        val quests = assignedQuests[id].orEmpty()

        if (quests.isEmpty()) return
        if (!quests.all { it.requirement.isComplete }) return

        val reward = config.questModule.allQuestsReward

        giveReward(player, reward)
        allQuestsRewardClaimed.add(id)
        store.setClaimedAllReward(id, true)

        player.showTitle(
            Title.title(
                MM.deserialize("<green><b>All Quests Completed!</b></green>"),
                MM.deserialize("<yellow>Reward: ${reward.description}</yellow>"),
            ),
        )
    }

    /**
     * Gives a reward to a player based on the specified quest reward.
     * @param player The player to receive the reward.
     * @param reward The reward to be given.
     */
    private fun giveReward(
        player: Player,
        reward: Quest.Reward,
    ) {
        if (reward.amount <= 0) return

        when (reward.type) {
            Material.EXPERIENCE_BOTTLE -> {
                player.giveExp(reward.amount, true)
            }

            else -> {
                val stack = ItemStack.of(reward.type, reward.amount)
                val leftover = player.inventory.addItem(stack)

                if (leftover.isNotEmpty()) {
                    leftover.values.forEach { player.world.dropItemNaturally(player.location, it) }
                }
            }
        }
    }

    /**
     * Assigns quests to a player upon joining the server.
     * @param event The player join event containing player information.
     */
    private fun assignInitQuests(event: PlayerJoinEvent) {
        val player = event.player
        val pool = config.questModule.quests
        val easy = pool.filter { it.difficulty == Quest.Difficulty.EASY }.shuffled().take(2)
        val medium = pool.filter { it.difficulty == Quest.Difficulty.MEDIUM }.shuffled().take(2)
        val hard = pool.filter { it.difficulty == Quest.Difficulty.HARD }.shuffled().take(1)
        val picked = (easy + medium + hard).map { it.copy(requirement = it.requirement.copy()) }
        val id = player.uniqueId.toKotlinUuid()

        assignedQuests[id] = picked
        allQuestsRewardClaimed.remove(id)
        store.setClaimedAllReward(id, false)
    }

    @Serializable
    data class TargetKey(
        val kind: Kind,
        val id: String,
    ) {
        @Serializable
        enum class Kind { ENTITY_TYPE, MATERIAL }

        companion object {
            fun entity(type: EntityType) = TargetKey(Kind.ENTITY_TYPE, type.name)

            fun material(mat: Material) = TargetKey(Kind.MATERIAL, mat.name)
        }

        fun matches(entityType: EntityType): Boolean = kind == Kind.ENTITY_TYPE && id == entityType.name

        fun matches(material: Material): Boolean = kind == Kind.MATERIAL && id == material.name
    }

    /** Represents a quest with its difficulty, requirement, and reward. */
    @Serializable
    data class Quest(
        var difficulty: Difficulty,
        var requirement: Requirement,
        var reward: Reward,
    ) {
        /** Represents the difficulty levels of a quest. */
        enum class Difficulty(
            val description: String,
        ) {
            EASY("<green><b>Easy Quest</b></green>"),
            MEDIUM("<yellow><b>Medium Quest</b></yellow>"),
            HARD("<red><b>Hard Quest</b></red>"),
        }

        /** Represents a requirement for completing a quest. */
        @Serializable
        data class Requirement(
            val target: TargetKey,
            val targetAmount: Int,
        ) {
            constructor(entityType: EntityType, targetAmount: Int) :
                this(TargetKey.entity(entityType), targetAmount)

            constructor(material: Material, targetAmount: Int) :
                this(TargetKey.material(material), targetAmount)

            /** Tracks the current progress towards completing the requirement. */
            var currentProgress: Int = 0

            /**
             * Indicates whether the quest requirement has been completed.
             * @return true if the current progress meets or exceeds the target amount, false otherwise.
             */
            val isComplete: Boolean get() = currentProgress >= targetAmount

            /**
             * Generates a human-readable description of the requirement.
             * @return String describing the requirement.
             */
            val description: String
                get() =
                    when (target.kind) {
                        TargetKey.Kind.ENTITY_TYPE -> {
                            val entityType = EntityType.valueOf(target.id)
                            val entityName =
                                entityType.name
                                    .lowercase()
                                    .split("_")
                                    .joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }

                            val verb =
                                when (entityType) {
                                    EntityType.CHICKEN, EntityType.COW, EntityType.PIG,
                                    EntityType.SHEEP, EntityType.RABBIT,
                                    -> "Collect"

                                    else -> "Kill"
                                }

                            if (targetAmount == 1) "$verb 1 $entityName" else "$verb $targetAmount ${entityName}s"
                        }

                        TargetKey.Kind.MATERIAL -> {
                            val material = Material.valueOf(target.id)
                            val materialName =
                                material.name
                                    .lowercase()
                                    .split("_")
                                    .joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }

                            if (targetAmount ==
                                1
                            ) {
                                "Collect 1 $materialName"
                            } else {
                                "Collect $targetAmount ${materialName}s"
                            }
                        }
                    }
        }

        /** Represents a reward given upon completing a quest. */
        @Serializable
        data class Reward(
            val type: Material,
            val amount: Int,
        ) {
            /**
             * Automatically generates a description for the reward based on type and amount.
             * @return Formatted string description of the reward.
             */
            val description: String get() = generateDescription()

            /**
             * Generates a human-readable description of the reward.
             * @return String describing the reward.
             */
            private fun generateDescription(): String {
                val formattedMaterialName =
                    type.name
                        .lowercase()
                        .split("_")
                        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

                return when {
                    type == Material.EXPERIENCE_BOTTLE -> "$amount XP"
                    amount == 1 -> "1 $formattedMaterialName"
                    else -> "$amount ${formattedMaterialName}s"
                }
            }
        }
    }

    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var quests: List<Quest> =
            listOf(
                Quest(
                    Quest.Difficulty.EASY,
                    Quest.Requirement(EntityType.ZOMBIE, 10),
                    Quest.Reward(Material.EXPERIENCE_BOTTLE, 100),
                ),
                Quest(
                    Quest.Difficulty.EASY,
                    Quest.Requirement(EntityType.SKELETON, 5),
                    Quest.Reward(Material.ARROW, 32),
                ),
                Quest(
                    Quest.Difficulty.MEDIUM,
                    Quest.Requirement(Material.DIAMOND, 5),
                    Quest.Reward(Material.DIAMOND, 1),
                ),
                Quest(
                    Quest.Difficulty.MEDIUM,
                    Quest.Requirement(Material.COBBLESTONE, 64),
                    Quest.Reward(Material.EMERALD, 3),
                ),
                Quest(
                    Quest.Difficulty.HARD,
                    Quest.Requirement(EntityType.ENDER_DRAGON, 1),
                    Quest.Reward(Material.EXPERIENCE_BOTTLE, 500),
                ),
            ),
        var allQuestsReward: Quest.Reward = Quest.Reward(Material.EXPERIENCE_BOTTLE, 250),
    )
}
