/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.util.Vector
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt

/** Represents a module handling dimension mechanics within the system. */
class DimensionsModule : ModuleInterface<DimensionsModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    companion object {
        private val ADJACENT_DIRECTIONS = listOf<Vector>(
            Vector(1, 0, 0), Vector(-1, 0, 0),
            Vector(0, 1, 0), Vector(0, -1, 0),
            Vector(0, 0, 1), Vector(0, 0, -1)
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerPortalEvent) {
        if (!enabled()) return

        val player = event.player
        if (event.cause == TeleportCause.NETHER_PORTAL) {
            when (player.world.environment) {
                World.Environment.NORMAL -> {
                    event.canCreatePortal = true
                }

                World.Environment.NETHER -> {
                    event.canCreatePortal = false
                    val overworld = instance.server.worlds.find { it.environment == World.Environment.NORMAL }
                    if (overworld != null) {
                        val overworldCoords = convertNetherToOverworldCoords(player.location)
                        if (!hasPortalNearby(overworld, overworldCoords.x, overworldCoords.y, overworldCoords.z)) {
                            extinguishPortal(player.location)
                            player.sendActionBar("No link to the portal counterpart in the Overworld".fireFmt().mm())
                        }
                    }
                }

                else -> return
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: EntityPortalEvent) {
        if (!enabled()) return

        event.canCreatePortal = false
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockIgniteEvent) {
        if (!enabled()) return

        val block = event.block
        val world = block.world

        if (world.environment != World.Environment.NETHER) return
        if (!isPortalFrame(block)) return

        val overworldCoords = convertNetherToOverworldCoords(block.location)
        val overworld = instance.server.worlds.find { it.environment == World.Environment.NORMAL } ?: return

        if (!hasPortalNearby(overworld, overworldCoords.x, overworldCoords.y, overworldCoords.z)) {
            event.isCancelled = true
            if (event.player != null) {
                event.player?.sendActionBar("Cannot create new link to the Overworld from the Nether".fireFmt().mm())
            }
        }
    }

    /**
     * Converts Nether coordinates to Overworld coordinates.
     * @param netherCoords The location in the Nether.
     * @return A Vector representing the corresponding Overworld coordinates.
     */
    private fun convertNetherToOverworldCoords(netherCoords: Location): Vector {
        return Vector(netherCoords.x * 8, netherCoords.y, netherCoords.z * 8)
    }

    /**
     * Checks if the block is part of a valid nether portal frame.
     * @param block The block to check.
     * @return True if the block is part of a valid portal frame.
     */
    private fun isPortalFrame(block: Block): Boolean {
        var obsidianCount = 0
        for (dir in ADJACENT_DIRECTIONS) {
            val relative = block.getRelative(dir.blockX, dir.blockY, dir.blockZ)
            if (relative.type == Material.OBSIDIAN) obsidianCount++
        }
        return obsidianCount >= 2
    }

    /**
     * Checks if there's a portal near the specified coordinates.
     * @param world The world to check in.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     * @return True if a portal exists within the radius.
     */
    private fun hasPortalNearby(world: World, x: Double, y: Double, z: Double): Boolean {
        val portalSearchRadius = config.portalSearchRadius
        val radiusSquared = portalSearchRadius * portalSearchRadius
        val searchRadius = portalSearchRadius.toInt()
        val centerX = x.toInt()
        val centerY = y.toInt()
        val centerZ = z.toInt()

        for (dy in -searchRadius..searchRadius) {
            val distanceYSquared = dy * dy
            if (distanceYSquared > radiusSquared) continue

            for (dx in -searchRadius..searchRadius) {
                val distanceXYSquared = distanceYSquared + dx * dx
                if (distanceXYSquared > radiusSquared) continue

                for (dz in -searchRadius..searchRadius) {
                    val distanceSquared = distanceXYSquared + dz * dz
                    if (distanceSquared > radiusSquared) continue

                    val block = world.getBlockAt(centerX + dx, centerY + dy, centerZ + dz)
                    if (block.type == Material.NETHER_PORTAL) return true
                }
            }
        }
        return false
    }

    /**
     * Extinguishes the Nether portal at the given location.
     * @param location The location of the portal.
     */
    private fun extinguishPortal(location: Location) {
        val visited = mutableSetOf<Block>()
        val queue = ArrayDeque<Block>()
        for (dir in ADJACENT_DIRECTIONS) {
            val block = location.block.getRelative(dir.blockX, dir.blockY, dir.blockZ)
            if (block.type == Material.NETHER_PORTAL) {
                queue.add(block)
                visited.add(block)
            }
        }
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            current.type = Material.AIR
            for (dir in ADJACENT_DIRECTIONS) {
                val neighbor = current.getRelative(dir.blockX, dir.blockY, dir.blockZ)
                if (neighbor.type == Material.NETHER_PORTAL && neighbor !in visited) {
                    queue.add(neighbor)
                    visited.add(neighbor)
                }
            }
        }
    }

    data class Config(
        override var enabled: Boolean = true,
        val portalSearchRadius: Double = 128.0,
    ) : ModuleInterface.Config
}