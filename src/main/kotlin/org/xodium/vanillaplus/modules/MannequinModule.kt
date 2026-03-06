package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.PlayerInventory
import org.xodium.vanillaplus.dialogs.MannequinDialog.dialog
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.pdcs.MannequinPDC.owner

/** Represents a module handling mannequin mechanics within the system. */
internal object MannequinModule : ModuleInterface {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return

        val player = event.player

        when (val entity = event.rightClicked) {
            is Mannequin -> {
                if (!player.isSneaking) return
                if (entity.owner != player.uniqueId) return

                player.showDialog(entity.dialog())
                event.isCancelled = true
            }

            is Villager -> {
                if (player.inventory.itemInMainHand.type != config.mannequinModule.triggerItem) return

                consumeItem(player.inventory)
                villagerToMannequin(player, entity)
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun on(event: EntityDeathEvent) {
        val mannequin = event.entity as? Mannequin? ?: return

        event.drops.apply {
            clear()
            addAll(listOf(*mannequin.equipment.armorContents))
            add(mannequin.equipment.itemInMainHand)
            add(mannequin.equipment.itemInOffHand)
        }
    }

    /**
     * Converts the interacted villager into a mannequin entity.
     * @param villager The villager to convert.
     */
    private fun villagerToMannequin(
        player: Player,
        villager: Villager,
    ) {
        val mannequin = villager.world.spawnEntity(villager.location, EntityType.MANNEQUIN) as Mannequin

        mannequin.customName(villager.customName())
        mannequin.owner = player.uniqueId

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
