package org.xodium.vanillaplus.modules

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.event.world.PortalCreateEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt

/** Represents a module handling dimension mechanics within the system. */
class DimensionsModule : ModuleInterface<DimensionsModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PortalCreateEvent) {
        if (!enabled()) return

        if (event.world.environment == World.Environment.NETHER
            && event.reason == PortalCreateEvent.CreateReason.FIRE
        ) {
            val portalBlocks = event.blocks
            if (portalBlocks.isEmpty()) return

            val portalLocation = portalBlocks.first().location
            val overworld =
                instance.server.worlds.firstOrNull { it.environment == World.Environment.NORMAL } ?: return
            if (!hasLinkedOverworldPortal(overworld, portalLocation)) {
                event.isCancelled = true

                val player = event.entity as? Player
                player?.sendActionBar(
                    "You can only light a portal in the Nether if a corresponding portal already exists in the Overworld".fireFmt()
                        .mm()
                )
            }
        }
    }

    /**
     * Checks if there is a linked portal in the Overworld near the given Nether portal location.
     * @param overworld The Overworld world instance.
     * @param netherLocation The location of the Nether portal block.
     * @return True if a linked portal exists, false otherwise.
     */
    private fun hasLinkedOverworldPortal(overworld: World, netherLocation: Location): Boolean {
        val overworldX = netherLocation.blockX * 8
        val overworldZ = netherLocation.blockZ * 8
        val searchRadius = 16
        return sequence {
            for (x in (overworldX - searchRadius)..(overworldX + searchRadius)) {
                for (z in (overworldZ - searchRadius)..(overworldZ + searchRadius)) {
                    for (y in overworld.minHeight until overworld.maxHeight) {
                        yield(overworld.getBlockAt(x, y, z))
                    }
                }
            }
        }.any { it.type == Material.NETHER_PORTAL }
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}