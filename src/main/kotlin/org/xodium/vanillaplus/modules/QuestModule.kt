/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
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
import org.xodium.vanillaplus.data.QuestData
import org.xodium.vanillaplus.enums.QuestDifficulty
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.roseFmt
import org.xodium.vanillaplus.utils.Utils.tryCatch

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
        if (event.view.title() == config.inventoryTitle.mm()) event.isCancelled = true
    }

    /**
     * Creates an inventory for quests.
     * @param player the [Player] for whom the inventory is created.
     * @return an [Inventory] with 5 slots, each containing a quest item.
     */
    private fun quests(player: Player): Inventory {
        var playerData = PlayerData.get(player)
        if (playerData.quests.isEmpty()) {
            val newQuests = generateQuestsForPlayer()
            playerData = playerData.copy(quests = newQuests)
            PlayerData.update(player, playerData)
        }

        return instance.server.createInventory(null, InventoryType.HOPPER, config.inventoryTitle.mm()).apply {
            playerData.quests.forEachIndexed { index, quest -> setItem(index, questItem(quest)) }
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
        val lore = listOf(
            "<b>\uD83D\uDCDD</b> ${quest.task}".roseFmt(),
            "<b>\uD83C\uDF81</b> ${quest.reward}".roseFmt(),
        )
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
            QuestData(QuestDifficulty.EASY, easyQuests[0].first, easyQuests[0].second),
            QuestData(QuestDifficulty.EASY, easyQuests[1].first, easyQuests[1].second),
            QuestData(QuestDifficulty.MEDIUM, mediumQuests[0].first, mediumQuests[0].second),
            QuestData(QuestDifficulty.MEDIUM, mediumQuests[1].first, mediumQuests[1].second),
            QuestData(QuestDifficulty.HARD, hardQuest[0].first, hardQuest[0].second),
        )
    }

    data class Config(
        override var enabled: Boolean = true,
        var inventoryTitle: String = "<b>Quests</b>".fireFmt(),
        var questPool: Map<QuestDifficulty, List<Pair<String, String>>> = mapOf(
            QuestDifficulty.EASY to listOf(
                "Mine 64 Cobblestone" to "1 Experience Bottle",
                "Craft 5 Stone Swords" to "2 Experience Bottles",
                "Harvest 32 Wheat" to "1 Experience Bottle",
                "Smelt 10 Iron Ore" to "2 Experience Bottles",
            ),
            QuestDifficulty.MEDIUM to listOf(
                "Kill 10 Zombies" to "5 Experience Bottles",
                "Find a Diamond" to "4 Experience Bottles",
                "Brew a Potion of Swiftness" to "6 Experience Bottles",
                "Enter the Nether" to "8 Experience Bottles",
            ),
            QuestDifficulty.HARD to listOf(
                "Defeat the Ender Dragon" to "64 Experience Bottles",
                "Obtain a Netherite Ingot" to "32 Experience Bottles",
                "Cure a Zombie Villager" to "48 Experience Bottles",
                "Defeat a Wither" to "50 Experience Bottles",
            )
        )
    ) : ModuleInterface.Config
}