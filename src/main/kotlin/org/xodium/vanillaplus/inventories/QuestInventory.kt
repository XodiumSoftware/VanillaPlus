package org.xodium.vanillaplus.inventories

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Represents the inventory for quest-related items and interactions. */
internal object QuestInventory : InventoryHolder {
    private val _inventory: Inventory = instance.server.createInventory(this, 9)

    override fun getInventory(): Inventory = _inventory
}
