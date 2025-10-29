package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.inventories.ArmorStandInventory
import org.xodium.vanillaplus.inventories.ArmorStandInventory.Companion.BOOTS_SLOT
import org.xodium.vanillaplus.inventories.ArmorStandInventory.Companion.CHESTPLATE_SLOT
import org.xodium.vanillaplus.inventories.ArmorStandInventory.Companion.HELMET_SLOT
import org.xodium.vanillaplus.inventories.ArmorStandInventory.Companion.LEGGINGS_SLOT
import org.xodium.vanillaplus.inventories.ArmorStandInventory.Companion.MAIN_HAND_SLOT
import org.xodium.vanillaplus.inventories.ArmorStandInventory.Companion.OFF_HAND_SLOT

/** Represents a module handling armour stand mechanics within the system. */
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
            val armorStandInventory = inventory.holder as ArmorStandInventory

            val equipmentSlots =
                setOf(
                    HELMET_SLOT,
                    CHESTPLATE_SLOT,
                    LEGGINGS_SLOT,
                    BOOTS_SLOT,
                    MAIN_HAND_SLOT,
                    OFF_HAND_SLOT,
                )

            if (event.slot in equipmentSlots) {
                instance.server.scheduler.runTask(
                    instance,
                    Runnable { armorStandInventory.handleClick(event.slot) },
                )
                return
            }

            event.isCancelled = true
            armorStandInventory.handleClick(event.slot)
        }
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
