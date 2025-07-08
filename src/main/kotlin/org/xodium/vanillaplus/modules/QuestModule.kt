/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.*
import org.xodium.vanillaplus.enums.QuestDifficulty
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.roseFmt
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** Represents a module handling quests mechanics within the system. */
class QuestModule : ModuleInterface<QuestModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    override fun cmds(): CommandData? {
        return CommandData(
            listOf(
                Commands.literal("quests")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx ->
                        ctx.tryCatch {
                            val player = it.sender as Player
                            player.openInventory(quests(player))
                        }
                    }
            ),
            "Lists all quests",
            listOf("q")
        )
    }

    override fun perms(): List<Permission> {
        return listOf(
            Permission(
                "${instance::class.simpleName}.quests.use".lowercase(),
                "Allows use of the quests command",
                PermissionDefault.TRUE
            )
        )
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        if (!enabled()) return
        if (event.view.title() != config.menuTitle.mm()) return

        event.isCancelled = true

        if (event.isLeftClick) {
            val player = event.whoClicked as Player
            val playerData = PlayerData.get(player)
            val quest = playerData.quests.getOrNull(event.slot) ?: return
            if (quest.completed && !quest.claimed) {
                val reward = quest.reward
                player.inventory.addItem(ItemStack.of(reward.material, reward.amount))
                quest.claimed = true
                PlayerData.update(player, playerData)
                player.openInventory(quests(player))
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: EntityDeathEvent) {
        if (!enabled()) return
        if (event.entity.killer == null) return

        val player = event.entity.killer!!
        handleQuestProgress(player, "Kill", event.entityType)
        handleQuestProgress(player, "Defeat", event.entityType)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockBreakEvent) {
        if (!enabled()) return

        val player = event.player
        handleQuestProgress(player, "Mine", event.block.type)
        handleQuestProgress(player, "Harvest", event.block.type)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: CraftItemEvent) {
        if (!enabled()) return

        val player = event.whoClicked as Player
        handleQuestProgress(player, "Craft", event.recipe.result.type, event.recipe.result.amount)
    }

    /**
     * Creates an inventory for quests.
     * @param player the [Player] for whom the inventory is created.
     * @return an [Inventory] with 5 slots, each containing a quest item.
     */
    private fun quests(player: Player): Inventory {
        var playerData = PlayerData.get(player)
        if (shouldResetQuests()) {
            instance.server.onlinePlayers.forEach { player ->
                val playerData = PlayerData.get(player)
                PlayerData.update(player, playerData.copy(quests = generateQuestsForPlayer()))
            } //TODO: check for offline players instead since we want to reset quests for all players
            config.lastReset = System.currentTimeMillis()
        }

        if (playerData.quests.isEmpty()) {
            playerData = playerData.copy(quests = generateQuestsForPlayer())
            PlayerData.update(player, playerData)
        }

        return instance.server.createInventory(null, InventoryType.HOPPER, config.menuTitle.mm()).apply {
            playerData.quests.forEachIndexed { index, quest -> setItem(index, questItem(quest)) }
        }
    }

    /**
     * Checks if quests should be reset based on the last reset time.
     * Resets quests every Monday at 00:00.
     * @return true if quests should be reset, false otherwise.
     */
    private fun shouldResetQuests(): Boolean {
        val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime()
        val last = Instant.ofEpochMilli(config.lastReset).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val nextReset = last.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0)
        return now.isAfter(nextReset)
    }

    /**
     * Creates a quest item with a specific material.
     * @param quest The [QuestData] to create the item for.
     * @return an [ItemStack] representing the quest item.
     */
    @Suppress("UnstableApiUsage")
    private fun questItem(quest: QuestData): ItemStack {
        val material = if (quest.completed) Material.ENCHANTED_BOOK else Material.WRITABLE_BOOK
        val name = quest.difficulty.title
        val lore = mutableListOf(
            "<b>\uD83D\uDCDD</b> ${quest.task}".roseFmt(),
            "<b>\uD83C\uDF81</b> ${quest.reward}".roseFmt(),
            ""
        )
        when {
            quest.claimed -> lore.add("<b><green>✔</green> Reward Claimed</b>")
            quest.completed -> lore.add("<b><green>✔</green> Click to claim your reward!</b>")
            else -> {
                val progress = quest.progress
                val required = quest.task.amount
                val progressBar = createProgressBar(progress, required)
                lore.add("<b>⏳</b> $progressBar ($progress/$required)".roseFmt())
            }
        }
        lore.add("")
        lore.add("<b>❗</b> Quests reset on each Monday at 00:00".fireFmt())

        return ItemStack.of(material).apply {
            setData(DataComponentTypes.ITEM_NAME, name.mm())
            setData(DataComponentTypes.LORE, ItemLore.lore(lore.mm()))
        }
    }

    /**
     * Generates a list of quests for the player.
     * @return a list of [QuestData] representing the player's quests.
     */
    private fun generateQuestsForPlayer(): List<QuestData> {
        val easyQuests = config.questPool[QuestDifficulty.EASY]!!.shuffled().take(2)
        val mediumQuests = config.questPool[QuestDifficulty.MEDIUM]!!.shuffled().take(2)
        val hardQuest = config.questPool[QuestDifficulty.HARD]!!.shuffled().take(1)

        val allQuests = easyQuests + mediumQuests + hardQuest
        val difficulties = listOf(
            QuestDifficulty.EASY, QuestDifficulty.EASY,
            QuestDifficulty.MEDIUM, QuestDifficulty.MEDIUM,
            QuestDifficulty.HARD
        )

        return allQuests.mapIndexed { index, (task, reward) ->
            QuestData(
                difficulty = difficulties[index],
                task = QuestTaskData(task.action, task.target, task.amount, task.uuid),
                reward = QuestRewardData(reward.material, reward.amount)
            )
        }
    }

    /**
     * Handles quest progress for a player based on an action.
     * @param player The player performing the action.
     * @param action The action performed (e.g., "Kill", "Mine").
     * @param target The target of the action (e.g., [EntityType.ZOMBIE], [Material.COBBLESTONE]).
     * @param amount The amount to add to the progress.
     */
    private fun handleQuestProgress(player: Player, action: String, target: Any, amount: Int = 1) {
        val playerData = PlayerData.get(player)
        var questsUpdated = false

        playerData.quests.forEach { quest ->
            if (!quest.completed) {
                val questTask = quest.task
                if (questTask.action == action && questTask.target == target) {
                    quest.progress += amount
                    if (quest.progress >= questTask.amount) quest.completed = true
                    questsUpdated = true
                }
            }
        }

        if (questsUpdated) PlayerData.update(player, playerData)
    }

    /**
     * Creates a progress bar string.
     * @param current The current progress.
     * @param max The maximum value for the progress.
     * @param length The total length of the progress bar in characters.
     * @return A formatted string representing the progress bar.
     */
    private fun createProgressBar(current: Int, max: Int, length: Int = 10): String {
        if (max <= 0) return ""
        val percentage = (current.toDouble() / max).coerceIn(0.0, 1.0)
        val filledLength = (percentage * length).toInt()
        val emptyLength = length - filledLength
        val filledChar = "■"
        return "<green>${filledChar.repeat(filledLength)}</green><gray>${filledChar.repeat(emptyLength)}</gray>"
    }

    data class Config(
        override var enabled: Boolean = true,
        var menuTitle: String = "<b>Quests</b>".fireFmt(),
        var lastReset: Long = 0L,
        var questPool: Map<QuestDifficulty, List<Pair<QuestTaskData, QuestRewardData>>> = mapOf(
            QuestDifficulty.EASY to listOf(
                QuestTaskData("Mine", Material.COBBLESTONE, 64) to QuestRewardData(Material.EXPERIENCE_BOTTLE, 1),
                QuestTaskData("Craft", Material.STONE_SWORD, 5) to QuestRewardData(Material.EXPERIENCE_BOTTLE, 2),
                QuestTaskData("Harvest", Material.WHEAT, 32) to QuestRewardData(Material.EXPERIENCE_BOTTLE, 1),
                QuestTaskData("Smelt", Material.IRON_ORE, 10) to QuestRewardData(Material.EXPERIENCE_BOTTLE, 2),
            ),
            QuestDifficulty.MEDIUM to listOf(
                QuestTaskData("Kill", EntityType.ZOMBIE, 10) to QuestRewardData(Material.EXPERIENCE_BOTTLE, 5),
                QuestTaskData("Find", Material.DIAMOND, 1) to QuestRewardData(Material.EXPERIENCE_BOTTLE, 4),
                QuestTaskData("Brew", Material.POTION, 1) to QuestRewardData(Material.EXPERIENCE_BOTTLE, 6),
                QuestTaskData("Trade", EntityType.VILLAGER, 3) to QuestRewardData(Material.EXPERIENCE_BOTTLE, 3),
            ),
            QuestDifficulty.HARD to listOf(
                QuestTaskData("Defeat", EntityType.ENDER_DRAGON, 1) to QuestRewardData(Material.EXPERIENCE_BOTTLE, 64),
                QuestTaskData("Obtain", Material.NETHERITE_INGOT, 1) to QuestRewardData(Material.EXPERIENCE_BOTTLE, 32),
                QuestTaskData("Cure", EntityType.ZOMBIE_VILLAGER, 1) to QuestRewardData(Material.EXPERIENCE_BOTTLE, 48),
                QuestTaskData("Defeat", EntityType.WITHER, 1) to QuestRewardData(Material.EXPERIENCE_BOTTLE, 50),
            )
        ),
    ) : ModuleInterface.Config
}