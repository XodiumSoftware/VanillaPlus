package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.ServerListPingEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a module handling MOTD mechanics within the system. */
internal object MotdModule : ModuleInterface {
    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: ServerListPingEvent) = motd(event)

    /**
     * Sets the MOTD for the server list ping event.
     * @param event The server list ping event.
     */
    private fun motd(event: ServerListPingEvent) =
        event.motd(
            config.motdFeature.motd
                .joinToString("\n")
                .mm(),
        )
}
