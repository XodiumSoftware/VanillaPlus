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
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.PlayerData
import org.xodium.vanillaplus.data.PlayerQuestsData
import org.xodium.vanillaplus.data.QuestData
import org.xodium.vanillaplus.enums.QuestDifficulty
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.roseFmt
import org.xodium.vanillaplus.utils.Utils.tryCatch
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        if (!enabled()) return
        if (event.view.title() != config.inventoryTitle.mm()) return

        event.isCancelled = true

        if (event.isLeftClick) {
            val player = event.whoClicked as Player
            val playerData = PlayerData.get(player)
            val quest = playerData.quests.list.getOrNull(event.slot) ?: return
            if (quest.completed && !quest.claimed) {
                val reward = findRewardForQuest(quest)
                if (reward != null) {
                    player.inventory.addItem(ItemStack.of(reward.material, reward.amount))
                    quest.claimed = true
                    PlayerData.update(player, playerData)
                    player.openInventory(quests(player))
                }
            }
        }
    }

    /**
     * Creates an inventory for quests.
     * @param player the [Player] for whom the inventory is created.
     * @return an [Inventory] with 5 slots, each containing a quest item.
     */
    private fun quests(player: Player): Inventory {
        var playerData = PlayerData.get(player)
        if (playerData.quests.list.isEmpty() || areQuestsExpired(playerData.quests.timestamp)) {
            val newQuests = generateQuestsForPlayer()
            val newPlayerQuests = PlayerQuestsData(list = newQuests, timestamp = System.currentTimeMillis())
            playerData = playerData.copy(quests = newPlayerQuests)
            PlayerData.update(player, playerData)
        }

        return instance.server.createInventory(null, InventoryType.HOPPER, config.inventoryTitle.mm()).apply {
            playerData.quests.list.forEachIndexed { index, quest -> setItem(index, questItem(quest)) }
        }
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
            else -> lore.add("<b><gray>✖</gray> In Progress</b>") //TODO: adjust based on size task to display a bar.
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
        return listOf(
            QuestData(QuestDifficulty.EASY, easyQuests[0].first, createRewardDescription(easyQuests[0].second)),
            QuestData(QuestDifficulty.EASY, easyQuests[1].first, createRewardDescription(easyQuests[1].second)),
            QuestData(QuestDifficulty.MEDIUM, mediumQuests[0].first, createRewardDescription(mediumQuests[0].second)),
            QuestData(QuestDifficulty.MEDIUM, mediumQuests[1].first, createRewardDescription(mediumQuests[1].second)),
            QuestData(QuestDifficulty.HARD, hardQuest[0].first, createRewardDescription(hardQuest[0].second)),
        )
    }

    /**
     * Creates a formatted description for a quest reward.
     * @param reward The [Config.QuestReward] to create the description for.
     * @return a formatted string describing the reward.
     */
    private fun createRewardDescription(reward: Config.QuestReward): String {
        val materialName = reward.material.name.replace('_', ' ').lowercase().replaceFirstChar { it.titlecase() }
        return "${reward.amount} $materialName${if (reward.amount > 1) "s" else ""}"
    }

    // TODO: add createTaskDescription method to format task descriptions

    /**
     * Checks if the quests have expired based on the weekly reset schedule.
     * @param timestamp The timestamp when the quests were last generated.
     * @return `true` if the quests should be reset, `false` otherwise.
     */
    private fun areQuestsExpired(timestamp: Long): Boolean {
        if (timestamp <= 0) return true

        val lastReset = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime()
        val nextReset = lastReset.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0)

        return now.isAfter(nextReset)
    }

    /**
     * Finds the configured reward for a given quest.
     * @param quest The quest to find the reward for.
     * @return The [Config.QuestReward] or null if not found.
     */
    private fun findRewardForQuest(quest: QuestData): Config.QuestReward? {
        return config.questPool[quest.difficulty]?.find { it.first == quest.task }?.second
    }

    data class Config(
        override var enabled: Boolean = true,
        var inventoryTitle: String = "<b>Quests</b>".fireFmt(),
        var questPool: Map<QuestDifficulty, List<Pair<QuestTask, QuestReward>>> = mapOf(
            QuestDifficulty.EASY to listOf(
                QuestTask("Mine", Material.COBBLESTONE, 64) to QuestReward(Material.EXPERIENCE_BOTTLE, 1),
                QuestTask("Craft", Material.STONE_SWORD, 5) to QuestReward(Material.EXPERIENCE_BOTTLE, 2),
                QuestTask("Harvest", Material.WHEAT, 32) to QuestReward(Material.EXPERIENCE_BOTTLE, 1),
                QuestTask("Smelt", Material.IRON_ORE, 10) to QuestReward(Material.EXPERIENCE_BOTTLE, 2),
            ),
            QuestDifficulty.MEDIUM to listOf(
                QuestTask("Kill", EntityType.ZOMBIE, 10) to QuestReward(Material.EXPERIENCE_BOTTLE, 5),
                QuestTask("Find", Material.DIAMOND, 1) to QuestReward(Material.EXPERIENCE_BOTTLE, 4),
                QuestTask("Brew", Material.POTION, 1) to QuestReward(Material.EXPERIENCE_BOTTLE, 6),
                // TODO: replace these kind of tasks
                QuestTask("Enter", "Nether", 1) to QuestReward(Material.EXPERIENCE_BOTTLE, 8),
            ),
            QuestDifficulty.HARD to listOf(
                QuestTask("Defeat", EntityType.ENDER_DRAGON, 1) to QuestReward(Material.EXPERIENCE_BOTTLE, 64),
                QuestTask("Obtain", Material.NETHERITE_INGOT, 1) to QuestReward(Material.EXPERIENCE_BOTTLE, 32),
                QuestTask("Cure", EntityType.ZOMBIE_VILLAGER, 1) to QuestReward(Material.EXPERIENCE_BOTTLE, 48),
                QuestTask("Defeat", EntityType.WITHER, 1) to QuestReward(Material.EXPERIENCE_BOTTLE, 50),
            )
        )
    ) : ModuleInterface.Config {
        data class QuestTask(val action: String, val material: Material, val amount: Int)
        data class QuestReward(val material: Material, val amount: Int)
    }
}