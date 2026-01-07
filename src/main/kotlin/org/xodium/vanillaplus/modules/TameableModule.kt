package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.PlayerUtils.getLeashedEntity

/** Represents a module handling tameable mechanics within the system. */
internal object TameableModule : ModuleInterface {
    @EventHandler
    fun on(event: PlayerInteractEntityEvent) = handleInteractEntity(event)

    /**
     * Handles the interaction event when a player interacts with another entity.
     * @param event The [PlayerInteractEntityEvent] triggered on entity interaction.
     */
    private fun handleInteractEntity(event: PlayerInteractEntityEvent) {
        val source = event.player
        val target = event.rightClicked as? Player ?: return

        if (source == target) return
        if (source.inventory.itemInMainHand.type != Material.LEAD) return

        val pet = source.getLeashedEntity() ?: return

        if (!pet.isTamed || pet.owner != source) return

        pet.owner = target
        pet.setLeashHolder(target)

        event.isCancelled = true
    }

    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
