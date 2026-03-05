package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.PlayerInventory
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling mannequin mechanics within the system. */
internal object MannequinModule : ModuleInterface {
    @EventHandler
    fun on(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.rightClicked.type != EntityType.VILLAGER) return
        if (event.player.inventory.itemInMainHand.type != config.mannequinModule.triggerItem) return

        consumeItem(event.player.inventory)
        villagerToMannequin(event.rightClicked as Villager)
    }

    /**
     * Converts the interacted villager into a mannequin entity.
     * @param villager The villager to convert.
     */
    private fun villagerToMannequin(villager: Villager) {
        val mannequin = villager.world.spawnEntity(villager.location, EntityType.MANNEQUIN) as Mannequin

        mannequin.customName(villager.customName())
        mannequin.isCustomNameVisible = villager.isCustomNameVisible

        villager.remove()
    }

    /**
     * Consumes one instance of the configured trigger item from the player's main hand.
     * @param inventory The player's inventory.
     */
    private fun consumeItem(inventory: PlayerInventory) {
        val item = inventory.itemInMainHand

        if (item.amount <= 1) {
            inventory.setItemInMainHand(null)
            return
        }

        item.amount -= 1
        inventory.setItemInMainHand(item)
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var triggerItem: Material = Material.TOTEM_OF_UNDYING,
    )
}
