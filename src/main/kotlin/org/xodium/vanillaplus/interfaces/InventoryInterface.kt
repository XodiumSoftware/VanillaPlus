package org.xodium.vanillaplus.interfaces

import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a contract for inventories within the system. */
internal interface InventoryInterface : InventoryHolder {
    /**
     * Initializes the quest inventory by filling it with placeholder items.
     * @param material The material to fill the inventory with.
     * @param customName The custom name to assign to each item. Defaults to an empty component.
     */
    @Suppress("UnstableApiUsage")
    fun Inventory.fill(
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
