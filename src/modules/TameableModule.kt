package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.xodium.vanillaplus.interfaces.ModuleConfigInterface
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.PlayerUtils.getLeashedEntity
import org.xodium.vanillaplus.utils.Utils.configDelegate

/** Represents a module handling tameable mechanics within the system. */
internal object TameableModule : ModuleInterface {
    override val config by configDelegate { Config() }

    @EventHandler
    fun on(event: PlayerInteractEntityEvent) {
        transferPetOwnership(event.player, event.rightClicked as? Player ?: return)
        event.isCancelled = true
    }

    /**
     * Transfers ownership of a tamed, leashed entity from [source] to [target].
     * Requires [source] to hold a lead in their main hand and be the current owner of the pet.
     * @param source The player initiating the transfer.
     * @param target The player receiving ownership.
     */
    private fun transferPetOwnership(
        source: Player,
        target: Player,
    ) {
        if (source.inventory.itemInMainHand.type != Material.LEAD) return

        val pet = source.getLeashedEntity() ?: return

        if (!pet.isTamed || pet.owner != source) return

        pet.owner = target
        pet.setLeashHolder(target)
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        override var enabled: Boolean = false,
    ) : ModuleConfigInterface
}
