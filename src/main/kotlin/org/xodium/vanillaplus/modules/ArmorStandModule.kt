package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.inventories.ArmorStandInventory

/** Represents a module handling armor stand mechanics within the system. */
internal class ArmorStandModule : ModuleInterface<ArmorStandModule.Config> {
    override val config: Config = Config()

    @EventHandler
    fun on(event: PlayerInteractAtEntityEvent) {
        if (!enabled() ||
            event.rightClicked !is ArmorStand ||
            event.player.isSneaking ||
            event.player.inventory.itemInMainHand.type == Material.NAME_TAG
        ) {
            return
        }
        event.player.openInventory(ArmorStandInventory(event.rightClicked as ArmorStand).inventory)
        event.isCancelled = true
    }

    @EventHandler
    fun on(event: InventoryClickEvent) {
        val inventory = event.inventory
        val clickedInventory = event.clickedInventory

        if (inventory.holder is ArmorStandInventory && clickedInventory == inventory) {
            event.isCancelled = true

            val armorStandInventory = inventory.holder as ArmorStandInventory
            armorStandInventory.handleClick(event.slot)
        }
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
