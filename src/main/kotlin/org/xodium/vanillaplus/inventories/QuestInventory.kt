package org.xodium.vanillaplus.inventories

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents the inventory interface for quests. */
internal class QuestInventory : InventoryHolder {
    private val _inventory: Inventory by lazy {
        instance.server
            .createInventory(this, 9, MM.deserialize("<b><gradient:#FFA751:#FFE259>Quests</gradient></b>"))
            .apply {
                setItem(
                    0,
                    @Suppress("UnstableApiUsage")
                    ItemStack.of(Material.PAPER).apply {
                        setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize("<green><b>Easy Quest</b></green>"))
                        setData(
                            DataComponentTypes.LORE,
                            ItemLore.lore(
                                listOf(
                                    MM.deserialize("<gray>Requirement: ?</gray>"),
                                    MM.deserialize("<gray>Reward: ?</gray>"),
                                ),
                            ),
                        )
                    },
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
}
