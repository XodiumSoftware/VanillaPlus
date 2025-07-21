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
import kotlin.math.pow
import kotlin.math.sqrt

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
            val overworld = instance.server.getWorld("world") ?: return
            val portalCenter = event.blocks
                .map { it.location }
                .reduce { acc, loc -> acc.add(loc) }
                .multiply(1.0 / event.blocks.size)
            val existingPortal = findCorrespondingPortal(portalCenter, overworld)
            if (existingPortal == null) {
                event.isCancelled = true
                val player = event.entity as? Player ?: return
                player.sendActionBar("No corresponding active portal found in the Overworld!".fireFmt().mm())
            }
        }
    }

    /**
     * Searches for an existing Nether portal in the target world near the corresponding scaled location.
     * @param netherPortal The location of the portal in the Nether.
     * @param overworld The world to search in (should be the Overworld).
     * @param searchRadius The radius in blocks to search for portal blocks.
     * @return The closest portal block location if found, otherwise null.
     */
    private fun findCorrespondingPortal(
        netherPortal: Location,
        overworld: World,
        searchRadius: Int = config.portalSearchRadius
    ): Location? {
        val targetX = netherPortal.x * 8
        val targetZ = netherPortal.z * 8

        var closestPortal: Location? = null
        var closestDistance = Double.MAX_VALUE

        val startX = (targetX - searchRadius).toInt()
        val endX = (targetX + searchRadius).toInt()
        val startZ = (targetZ - searchRadius).toInt()
        val endZ = (targetZ + searchRadius).toInt()

        for (x in startX..endX) {
            for (z in startZ..endZ) {
                for (y in 0..overworld.maxHeight) {
                    val block = overworld.getBlockAt(x, y, z)
                    if (block.type == Material.NETHER_PORTAL) {
                        val dist = distance2D(targetX, targetZ, x.toDouble(), z.toDouble())
                        if (dist < closestDistance) {
                            closestDistance = dist
                            closestPortal = block.location
                        }
                    }
                }
            }
        }
        return closestPortal
    }

    /**
     * Calculates the 2D Euclidean distance between two points in the X-Z plane.
     * @param x1 The X-coordinate of the first point.
     * @param z1 The Z-coordinate of the first point.
     * @param x2 The X-coordinate of the second point.
     * @param z2 The Z-coordinate of the second point.
     * @return The distance between the two points.
     */
    private fun distance2D(x1: Double, z1: Double, x2: Double, z2: Double): Double {
        return sqrt((x1 - x2).pow(2) + (z1 - z2).pow(2))
    }

    data class Config(
        override var enabled: Boolean = true,
        var portalSearchRadius: Int = 128,
    ) : ModuleInterface.Config
}