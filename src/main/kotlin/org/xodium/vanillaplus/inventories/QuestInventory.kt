@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.inventories

import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents the inventory for quest-related items and interactions. */
internal object QuestInventory : InventoryHolder {
    private val _inventory: Inventory =
        instance.server.createInventory(this, 9, "<gradient:#CB2D3E:#EF473A>Quests</gradient>".mm()).apply {
            fill(Material.GRAY_STAINED_GLASS_PANE)
        }

    override fun getInventory(): Inventory = _inventory

    /**
     * Initializes the quest inventory by filling it with placeholder items.
     * @param material The material to fill the inventory with.
     * @param customName The custom name to assign to each item. Defaults to an empty component.
     */
    private fun Inventory.fill(
        material: Material,
        customName: Component = "".mm(),
    ) {
        for (i in 0 until size) {
            setItem(
                i,
                ItemStack.of(material).apply {
                    setData(DataComponentTypes.CUSTOM_NAME, customName)
                },
            )
        }
    }
}
