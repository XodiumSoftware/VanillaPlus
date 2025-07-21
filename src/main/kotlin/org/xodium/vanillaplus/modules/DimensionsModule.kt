package org.xodium.vanillaplus.modules

import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.event.world.PortalCreateEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling dimension mechanics within the system. */
class DimensionsModule : ModuleInterface<DimensionsModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PortalCreateEvent) {
        if (!enabled()) return

        if (event.world.environment == World.Environment.NETHER
            && event.reason == PortalCreateEvent.CreateReason.FIRE
        ) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerPortalEvent) {
        if (!enabled()) return

        if (event.cause == TeleportCause.NETHER_PORTAL) {
            if (event.player.world.environment == World.Environment.NETHER) {
                event.canCreatePortal = false
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: EntityPortalEvent) {
        if (!enabled()) return

        if (event.entity.world.environment == World.Environment.NETHER) {
            event.canCreatePortal = false
        }
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}