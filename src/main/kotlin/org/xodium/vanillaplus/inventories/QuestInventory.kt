package org.xodium.vanillaplus.inventories

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.modules.QuestModule
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents the inventory interface for quests. */
internal class QuestInventory : InventoryHolder {
    private val _inventory: Inventory by lazy {
        instance.server
            .createInventory(this, 9, MM.deserialize("<b><gradient:#FFA751:#FFE259>Quests</gradient></b>"))
            .apply {
                filler()
                setItem(
                    0,
                    createQuestItem(
                        Material.PAPER,
                        QuestModule.Quest.Difficulty.EASY.description,
                        "<gray>Requirement: ?</gray>",
                        "<gray>Reward: ?</gray>",
                    ),
                )
                setItem(
                    1,
                    createQuestItem(
                        Material.PAPER,
                        QuestModule.Quest.Difficulty.EASY.description,
                        "<gray>Requirement: ?</gray>",
                        "<gray>Reward: ?</gray>",
                    ),
                )
                setItem(
                    3,
                    createQuestItem(
                        Material.PAPER,
                        QuestModule.Quest.Difficulty.MEDIUM.description,
                        "<gray>Requirement: ?</gray>",
                        "<gray>Reward: ?</gray>",
                    ),
                )
                setItem(
                    4,
                    createQuestItem(
                        Material.PAPER,
                        QuestModule.Quest.Difficulty.MEDIUM.description,
                        "<gray>Requirement: ?</gray>",
                        "<gray>Reward: ?</gray>",
                    ),
                )
                setItem(
                    6,
                    createQuestItem(
                        Material.PAPER,
                        QuestModule.Quest.Difficulty.HARD.description,
                        "<gray>Requirement: ?</gray>",
                        "<gray>Reward: ?</gray>",
                    ),
                )
                setItem(
                    8,
                    createQuestItem(
                        Material.ENDER_EYE,
                        "<blue><b>Completing all quests reward</b></blue>",
                        "<gray>Requirement:</gray> <yellow>Complete all quests</yellow>",
                        "<gray>Reward: ?</gray>",
                    ),
                )
            }
    }

    override fun getInventory(): Inventory = _inventory

    /**
     * Handles inventory click events to prevent interaction with the quest inventory.
     * @param event The inventory click event to handle.
     */
    fun inventoryClick(event: InventoryClickEvent) {
        if (event.clickedInventory?.holder is QuestInventory) event.isCancelled = true
    }

    /**
     * Fills empty slots in the inventory with black stained-glass panes as fillers.
     * @receiver The inventory to fill.
     */
    private fun Inventory.filler() {
        for (i in 0 until size) {
            if (getItem(i) == null) {
                setItem(
                    i,
                    @Suppress("UnstableApiUsage")
                    ItemStack.of(Material.BLACK_STAINED_GLASS_PANE).apply {
                        setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize(""))
                    },
                )
            }
        }
    }

    /**
     * Helper function to create a quest item with the given parameters.
     * @param material The material for the item.
     * @param name The display name for the item.
     * @param line1 The first line of the lore.
     * @param line2 The second line of the lore.
     * @return The created ItemStack.
     */
    @Suppress("UnstableApiUsage")
    private fun createQuestItem(
        material: Material,
        name: String,
        line1: String,
        line2: String,
    ): ItemStack =
        ItemStack.of(material).apply {
            setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize(name))
            setData(
                DataComponentTypes.LORE,
                ItemLore.lore(
                    listOf(
                        MM.deserialize(line1),
                        MM.deserialize(line2),
                    ),
                ),
            )
        }
}
