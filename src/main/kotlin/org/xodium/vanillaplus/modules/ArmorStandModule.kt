package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityInteractEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.menus.ArmorStandMenu.menu

/** Represents a module handling armor stand mechanics within the system. */
internal object ArmorStandModule : ModuleInterface {
    @EventHandler
    fun on(event: EntityInteractEvent) = handleArmorStandInventory(event)

    /**
     * Handles the interaction with an ArmorStand's inventory.
     * @param event EntityInteractEvent The event triggered by the interaction.
     */
    private fun handleArmorStandInventory(event: EntityInteractEvent) {
        val armorStand = event.entity as? ArmorStand ?: return
        val player = event.entity as? Player ?: return

        if (player.isSneaking && Action.RIGHT_CLICK_BLOCK.equals(armorStand)) {
            event.isCancelled = true
            player.openInventory(armorStand.menu(player))
        }
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
