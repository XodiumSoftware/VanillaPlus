package org.xodium.vanillaplus.inventories

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.InventoryInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents the inventory for quest-related items and interactions. */
internal object QuestInventory : InventoryInterface {
    private val _inventory: Inventory =
        instance.server.createInventory(this, 9, "<gradient:#CB2D3E:#EF473A>Quests</gradient>".mm()).apply {
            fill(Material.GRAY_STAINED_GLASS_PANE)
            setItem(2, ItemStack.of(Material.PAPER))
            setItem(3, ItemStack.of(Material.PAPER))
            setItem(4, ItemStack.of(Material.PAPER))
            setItem(5, ItemStack.of(Material.PAPER))
            setItem(6, ItemStack.of(Material.PAPER))
        }

    override fun getInventory(): Inventory = _inventory
}
