package org.xodium.vanillaplus.features

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.ServerListPingEvent
import org.xodium.vanillaplus.interfaces.FeatureInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a feature handling MOTD mechanics within the system. */
internal object MotdFeature : FeatureInterface {
    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: ServerListPingEvent) =
        event.motd(
            config.motdFeature.motd
                .joinToString("\n")
                .mm(),
        )
}
