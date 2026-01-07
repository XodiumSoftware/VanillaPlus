package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling pet mechanics within the system. */
internal object PetModule : ModuleInterface {
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

    /**
     * Gets the first leashed entity owned by the player within the config radius.
     * @receiver The player whose leashed entity is to be found.
     * @param radius The radius within which to search for leashed entities.
     * @return The found tameable entity or `null` if none exists.
     */
    private fun Player.getLeashedEntity(radius: Double = 10.0): Tameable? =
        getNearbyEntities(radius, radius, radius)
            .filterIsInstance<Tameable>()
            .firstOrNull { it.isLeashed && it.leashHolder == player }

    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
