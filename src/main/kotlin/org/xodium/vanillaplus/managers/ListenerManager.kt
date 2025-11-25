package org.xodium.vanillaplus.managers

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.xodium.vanillaplus.features.CauldronFeature

/** Represents the event listener manager within the system. */
internal object ListenerManager : Listener {
    init {
        // TODO: register event?
    }

    @EventHandler
    fun on(event: PlayerInteractEvent) {
        CauldronFeature.cauldron(event)
    }
}
