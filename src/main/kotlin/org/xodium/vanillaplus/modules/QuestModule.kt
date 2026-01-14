@file:OptIn(ExperimentalUuidApi::class)

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import kotlinx.serialization.Serializable
import net.kyori.adventure.title.Title
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.inventories.QuestInventory
import org.xodium.vanillaplus.modules.QuestModule.TypeKey.Companion.entityType
import org.xodium.vanillaplus.modules.QuestModule.TypeKey.Companion.material
import org.xodium.vanillaplus.pdcs.PlayerPDC.allQuestsCompleted
import org.xodium.vanillaplus.pdcs.PlayerPDC.quests
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import org.xodium.vanillaplus.utils.Utils.MM
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import kotlin.uuid.ExperimentalUuidApi

/** Represents a module handling quest mechanics within the system. */
internal object QuestModule : ModuleInterface {
    private val questInventory = QuestInventory()

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

    init {
        val now = LocalDateTime.now(ZoneId.systemDefault())
        val nextMonday =
            now
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)

        val delayMillis =
            nextMonday.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val delayTicks = (delayMillis / 50).coerceAtLeast(1)

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                resetAllQuests()
                instance.server.scheduler.runTaskTimer(
                    instance,
                    Runnable { resetAllQuests() },
                    12096000L,
                    12096000L,
                )
            },
            delayTicks,
        )
    }

    /** Resets quests for all players on the server. */
    private fun resetAllQuests() {
        instance.server.onlinePlayers.forEach { player ->
            assignInitQuests(player)
            player.sendMessage(MM.deserialize("<green><b>Your weekly quests have been reset!</b></green>"))
        }
        instance.logger.info("Weekly quests reset completed for all players")
    }

    @EventHandler
    fun on(event: InventoryClickEvent) = questInventory.inventoryClick(event)

    @EventHandler
    fun on(event: PlayerJoinEvent) {
        if (event.player.quests.isNullOrEmpty()) assignInitQuests(event.player)
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: EntityDeathEvent) {
        incrementMatchingQuests(
            player = event.entity.killer ?: return,
            predicate = { it.type.matches(event.entityType) },
            incrementBy = 1,
        )
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: BlockBreakEvent) {
        incrementMatchingQuests(
            player = event.player,
            predicate = { it.type.matches(event.block.type) },
            incrementBy = 1,
        )
    }

    /**
     * Retrieves the list of quests assigned to a player.
     * @param player The player whose quests are to be retrieved.
     * @return List of quests assigned to the player.
     */
    fun getAssignedQuests(player: Player): List<Quest> {
        val stored = player.quests ?: return emptyList()
        val poolById = config.questModule.quests.associateBy { it.id }

        return stored.mapNotNull { (questId, progress) ->
            val base = poolById[questId] ?: return@mapNotNull null
            val reqCopy = base.requirement.copy()

            reqCopy.currentProgress = progress
            base.copy(requirement = reqCopy)
        }
    }

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

        val quests = getAssignedQuests(player)
        var changed = false

        val updatedProgress = player.quests?.toMutableMap() ?: mutableMapOf()

        quests
            .asSequence()
            .filter { !it.requirement.isComplete }
            .filter { predicate(it.requirement) }
            .forEach { quest ->
                val req = quest.requirement
                val before = req.currentProgress
                val after = (before + incrementBy).coerceAtMost(req.amount)

                if (after != before) {
                    updatedProgress[quest.id] = after
                    req.currentProgress = after
                    changed = true
                }

                if (req.amount in (before + 1)..after) {
                    giveReward(player, quest.reward)
                    player.showTitle(
                        Title.title(
                            MM.deserialize("<green><b>Quest Completed!</b></green>"),
                            MM.deserialize("<yellow>Reward: ${quest.reward.description}</yellow>"),
                        ),
                    )
                }
            }

        if (changed) {
            player.quests = updatedProgress
            completedAllQuestsReward(player)
        }
    }

    /**
     * Set of player UUIDs who have claimed the all-quests reward.
     * @param player The player to potentially give the reward to.
     */
    private fun completedAllQuestsReward(player: Player) {
        if (player.allQuestsCompleted) return

        val quests = getAssignedQuests(player)

        if (quests.isEmpty()) return
        if (!quests.all { it.requirement.isComplete }) return

        val reward = config.questModule.allQuestsReward

        giveReward(player, reward)
        player.allQuestsCompleted = true

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
                val leftover = player.inventory.addItem(ItemStack.of(reward.type, reward.amount))

                if (leftover.isNotEmpty()) {
                    leftover.values.forEach { player.world.dropItemNaturally(player.location, it) }
                }
            }
        }
    }

    /**
     * Assigns initial quests to a player.
     * @param player The player to whom quests should be assigned.
     */
    private fun assignInitQuests(player: Player) {
        val pool = config.questModule.quests
        val easy = pool.filter { it.difficulty == Quest.Difficulty.EASY }.shuffled().take(2)
        val medium = pool.filter { it.difficulty == Quest.Difficulty.MEDIUM }.shuffled().take(2)
        val hard = pool.filter { it.difficulty == Quest.Difficulty.HARD }.shuffled().take(1)
        val picked = (easy + medium + hard).map { it.copy(requirement = it.requirement.copy()) }

        player.quests = picked.associate { it.id to 0 }
        player.allQuestsCompleted = false
    }

    /** Represents a key identifying a quest target, either an entity type or material. */
    @Serializable
    data class TypeKey(
        val kind: Kind,
        val id: String,
    ) {
        /** Represents the kinds of targets for quests. */
        @Serializable
        enum class Kind { ENTITY_TYPE, MATERIAL }

        companion object {
            /**
             * Creates a TargetKey for an entity type.
             * @param entityType The entity type.
             */
            fun entityType(entityType: EntityType) = TypeKey(Kind.ENTITY_TYPE, entityType.name)

            /**
             * Creates a TargetKey for a material.
             * @param material The material.
             */
            fun material(material: Material) = TypeKey(Kind.MATERIAL, material.name)
        }

        /**
         * Checks if the target key matches the given entity type.
         * @param entityType The entity type to check against.
         * @return true if the target key matches the entity type, false otherwise.
         */
        fun matches(entityType: EntityType): Boolean = kind == Kind.ENTITY_TYPE && id == entityType.name

        /**
         * Checks if the target key matches the given material.
         * @param material The material to check against.
         * @return true if the target key matches the material, false otherwise.
         */
        fun matches(material: Material): Boolean = kind == Kind.MATERIAL && id == material.name
    }

    /** Represents a quest with its difficulty, requirement, and reward. */
    @Serializable
    data class Quest(
        var id: Int,
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
            val type: TypeKey,
            val amount: Int,
        ) {
            constructor(entityType: EntityType, targetAmount: Int) :
                this(entityType(entityType), targetAmount)

            constructor(material: Material, targetAmount: Int) :
                this(material(material), targetAmount)

            /** Tracks the current progress towards completing the requirement. */
            var currentProgress: Int = 0

            /**
             * Indicates whether the quest requirement has been completed.
             * @return true if the current progress meets or exceeds the target amount, false otherwise.
             */
            val isComplete: Boolean get() = currentProgress >= amount

            /**
             * Generates a human-readable description of the requirement.
             * @return String describing the requirement.
             */
            val description: String
                get() =
                    when (type.kind) {
                        TypeKey.Kind.ENTITY_TYPE -> {
                            val entityType = EntityType.valueOf(type.id)
                            val entityName =
                                entityType.name
                                    .lowercase()
                                    .split("_")
                                    .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

                            val verb =
                                when (entityType) {
                                    EntityType.CHICKEN, EntityType.COW, EntityType.PIG,
                                    EntityType.SHEEP, EntityType.RABBIT,
                                    -> "Collect"

                                    else -> "Kill"
                                }

                            if (amount == 1) "$verb 1 $entityName" else "$verb $amount ${entityName}s"
                        }

                        TypeKey.Kind.MATERIAL -> {
                            val material = Material.valueOf(type.id)
                            val materialName =
                                material.name
                                    .lowercase()
                                    .split("_")
                                    .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

                            if (amount ==
                                1
                            ) {
                                "Collect 1 $materialName"
                            } else {
                                "Collect $amount ${materialName}s"
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

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var quests: List<Quest> =
            listOf(
                Quest(
                    0,
                    Quest.Difficulty.EASY,
                    Quest.Requirement(EntityType.ZOMBIE, 10),
                    Quest.Reward(Material.EXPERIENCE_BOTTLE, 100),
                ),
                Quest(
                    1,
                    Quest.Difficulty.EASY,
                    Quest.Requirement(EntityType.SKELETON, 5),
                    Quest.Reward(Material.ARROW, 32),
                ),
                Quest(
                    2,
                    Quest.Difficulty.MEDIUM,
                    Quest.Requirement(Material.DIAMOND, 5),
                    Quest.Reward(Material.DIAMOND, 1),
                ),
                Quest(
                    3,
                    Quest.Difficulty.MEDIUM,
                    Quest.Requirement(Material.COBBLESTONE, 64),
                    Quest.Reward(Material.EMERALD, 3),
                ),
                Quest(
                    4,
                    Quest.Difficulty.HARD,
                    Quest.Requirement(EntityType.ENDER_DRAGON, 1),
                    Quest.Reward(Material.EXPERIENCE_BOTTLE, 500),
                ),
            ),
        var allQuestsReward: Quest.Reward = Quest.Reward(Material.EXPERIENCE_BOTTLE, 250),
    )
}
