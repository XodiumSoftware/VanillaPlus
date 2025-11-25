package org.xodium.vanillaplus.features

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockState
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.world.PortalCreateEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.FeatureInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import kotlin.math.pow
import kotlin.math.sqrt

/** Represents a feature handling dimension mechanics within the system. */
internal object DimensionsFeature : FeatureInterface {
    private val config: Config = Config()

    private const val NETHER_TO_OVERWORLD_RATIO = 8

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerPortalEvent) {
        if (event.cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            if (event.player.world.environment == World.Environment.NETHER) event.canCreatePortal = false
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: EntityPortalEvent) {
        if (event.entity.world.environment == World.Environment.NETHER) event.canCreatePortal = false
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PortalCreateEvent) {
        if (event.world.environment == World.Environment.NETHER &&
            event.reason == PortalCreateEvent.CreateReason.FIRE
        ) {
            if (findCorrespondingPortal(calcPortalCentre(event.blocks), getOverworld()) == null) {
                event.isCancelled = true
                val player = event.entity as? Player ?: return
                player.sendActionBar(config.i18n.portalCreationDenied.mm())
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
        searchRadius: Int = config.portalSearchRadius,
    ): Location? {
        val targetX = netherPortal.x * NETHER_TO_OVERWORLD_RATIO
        val targetZ = netherPortal.z * NETHER_TO_OVERWORLD_RATIO

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
    private fun distance2D(
        x1: Double,
        z1: Double,
        x2: Double,
        z2: Double,
    ): Double = sqrt((x1 - x2).pow(2) + (z1 - z2).pow(2))

    /**
     * Calculates the centre point of a portal structure by averaging the positions of its constituent blocks.
     * @param blockStates The list of [org.bukkit.block.BlockState]s representing the portal frame and portal blocks.
     * @return The [Location] representing the geometric centre of the portal.
     */
    private fun calcPortalCentre(blockStates: List<BlockState>): Location =
        blockStates
            .map { it.location }
            .reduce { acc, loc -> acc.add(loc) }
            .multiply(1.0 / blockStates.size)

    /**
     * Retrieves the Overworld instance or throws if not found.
     * @return The Overworld [World] object.
     * @throws IllegalStateException if the Overworld is not loaded.
     */
    private fun getOverworld(): World = instance.server.getWorld("world") ?: error("Overworld (world) is not loaded.")

    data class Config(
        var portalSearchRadius: Int = 128,
        var i18n: I18n = I18n(),
    ) {
        data class I18n(
            var portalCreationDenied: String =
                "<gradient:#CB2D3E:#EF473A>No corresponding active portal found in the Overworld!</gradient>",
        )
    }
}
