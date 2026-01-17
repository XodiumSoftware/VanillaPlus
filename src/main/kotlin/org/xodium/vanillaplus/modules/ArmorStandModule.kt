package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.entity.ArmorStand
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.menus.ArmorStandMenu.menu

/** Represents a module handling armor stand mechanics within the system. */
internal object ArmorStandModule : ModuleInterface {
    @EventHandler
    fun on(event: PlayerInteractAtEntityEvent) = handleArmorStandInventory(event)

    /**
     * Handles the interaction with an ArmorStand's inventory.
     * @param event EntityInteractEvent The event triggered by the interaction.
     */
    private fun handleArmorStandInventory(event: PlayerInteractAtEntityEvent) {
        val armorStand = event.rightClicked as? ArmorStand ?: return
        val player = event.player

        if (!player.isSneaking) return

        event.isCancelled = true
        armorStand.menu(player).open()
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
