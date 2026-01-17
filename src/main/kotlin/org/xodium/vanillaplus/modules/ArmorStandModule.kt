package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.MenuType
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils.MM

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

    /**
     * Creates a menu for the given ArmorStand and Player.
     * @receiver ArmorStand The ArmorStand for which the menu is created.
     * @param player Player The player for whom the menu is created.
     * @return InventoryView The created menu view.
     */
    @Suppress("UnstableApiUsage")
    private fun ArmorStand.menu(player: Player): InventoryView =
        MenuType
            .GENERIC_9X6
            .builder()
            .title(customName() ?: MM.deserialize(name))
            .build(player)
            .apply {
                topInventory
                    .apply {
                    }
            }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
