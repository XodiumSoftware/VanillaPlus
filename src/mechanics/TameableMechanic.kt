package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.PlayerUtils.getLeashedEntity

/** Represents a module handling tameable mechanics within the system. */
internal object TameableMechanic : ModuleInterface {
    @EventHandler
    fun on(event: PlayerInteractEntityEvent) {
        if (transferPetOwnership(event.player, event.rightClicked as? Player ?: return)) event.isCancelled = true
    }

    /**
     * Transfers ownership of a tamed, leashed entity from [source] to [target].
     * Requires [source] to hold a lead in their main hand and be the current owner of the pet.
     * @param source The player initiating the transfer.
     * @param target The player receiving ownership.
     * @return `true` if ownership was successfully transferred; `false` otherwise.
     */
    private fun transferPetOwnership(
        source: Player,
        target: Player,
    ): Boolean {
        if (source.inventory.itemInMainHand.type != Material.LEAD) return false

        val pet = source.getLeashedEntity() ?: return false

        if (!pet.isTamed || pet.owner != source) return false

        pet.owner = target
        pet.setLeashHolder(target)
        return true
    }
}
