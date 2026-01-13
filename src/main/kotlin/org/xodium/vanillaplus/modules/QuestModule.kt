@file:OptIn(ExperimentalUuidApi::class)

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.inventories.QuestInventory
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

/** Represents a module handling quest mechanics within the system. */
internal object QuestModule : ModuleInterface {
    private val questInventory = QuestInventory()
    val assignedQuests: MutableMap<Uuid, List<Quest>> = mutableMapOf()

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
    fun on(event: PlayerJoinEvent) = assignInitQuests(event)

    @EventHandler(ignoreCancelled = true)
    fun on(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        val killedType = event.entityType

        incrementMatchingQuests(
            player = killer,
            predicate = { req -> req.entityType != null && req.entityType == killedType },
            incrementBy = 1,
        )
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return
        val stack = event.item.itemStack
        val material = stack.type
        val amount = stack.amount

        incrementMatchingQuests(
            player = player,
            predicate = { req -> req.material != null && req.material == material },
            incrementBy = amount,
        )
    }

    /**
     * Retrieves the list of quests assigned to a player.
     * @param player The player whose quests are to be retrieved.
     * @return List of quests assigned to the player.
     */
    fun getAssignedQuests(player: Player): List<Quest> = assignedQuests[player.uniqueId.toKotlinUuid()].orEmpty()

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

        val quests = assignedQuests[player.uniqueId.toKotlinUuid()] ?: return

        quests
            .asSequence()
            .filter { q -> !q.requirement.isComplete }
            .filter { q -> predicate(q.requirement) }
            .forEach { q ->
                val req = q.requirement
                val before = req.currentProgress
                val after = (before + incrementBy).coerceAtMost(req.targetAmount)

                if (after != before) req.currentProgress = after
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

        assignedQuests[player.uniqueId.toKotlinUuid()] = picked
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
            val entityType: EntityType? = null,
            val material: Material? = null,
            val targetAmount: Int,
        ) {
            /**
             * Automatically generates a description for the requirement based on type and amount.
             * @return Formatted string description of the requirement.
             */
            val description: String get() = generateDescription()

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
            private fun generateDescription(): String =
                when {
                    entityType != null -> {
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

                        if (targetAmount == 1) {
                            "$verb 1 $entityName"
                        } else {
                            "$verb $targetAmount ${entityName}s"
                        }
                    }

                    material != null -> {
                        val materialName =
                            material.name
                                .lowercase()
                                .split("_")
                                .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

                        if (targetAmount == 1) {
                            "Collect 1 $materialName"
                        } else {
                            "Collect $targetAmount ${materialName}s"
                        }
                    }

                    else -> {
                        "Complete task ($targetAmount times)"
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
                    Quest.Requirement(EntityType.ZOMBIE, null, 10),
                    Quest.Reward(Material.EXPERIENCE_BOTTLE, 100),
                ),
                Quest(
                    Quest.Difficulty.EASY,
                    Quest.Requirement(EntityType.SKELETON, null, 5),
                    Quest.Reward(Material.ARROW, 32),
                ),
                Quest(
                    Quest.Difficulty.MEDIUM,
                    Quest.Requirement(null, Material.DIAMOND, 5),
                    Quest.Reward(Material.DIAMOND, 1),
                ),
                Quest(
                    Quest.Difficulty.MEDIUM,
                    Quest.Requirement(null, Material.COBBLESTONE, 64),
                    Quest.Reward(Material.EMERALD, 3),
                ),
                Quest(
                    Quest.Difficulty.HARD,
                    Quest.Requirement(EntityType.ENDER_DRAGON, null, 1),
                    Quest.Reward(Material.EXPERIENCE_BOTTLE, 500),
                ),
            ),
    )
}
