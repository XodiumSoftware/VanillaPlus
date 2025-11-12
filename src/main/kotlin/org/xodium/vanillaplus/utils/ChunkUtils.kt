@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.utils

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.DoubleChest
import org.bukkit.inventory.InventoryHolder
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/** Chunk utilities. */
internal object ChunkUtils {
    private val cache = ConcurrentHashMap<ChunkCoord, Boolean>()

    private data class ChunkCoord(
        val world: UUID,
        val x: Int,
        val z: Int,
    )

    /**
     * Find all container blocks in a given radius from a location.
     * @param location The location to search from.
     * @param radius The radius to search within.
     * @param containerTypes Set of valid container materials.
     * @param containerFilter An additional filter for containers.
     * @return List of container blocks found within the radius.
     */
    fun findContainersInRadius(
        location: Location,
        radius: Int,
        containerTypes: Set<Material>,
        containerFilter: (Block) -> Boolean = { true },
    ): List<Block> {
        val world = location.world
        val centerX = location.blockX
        val centerZ = location.blockZ
        val radiusSquared = radius * radius
        val minChunkX = (centerX - radius) shr 4
        val maxChunkX = (centerX + radius) shr 4
        val minChunkZ = (centerZ - radius) shr 4
        val maxChunkZ = (centerZ + radius) shr 4
        val containers = mutableListOf<Block>()

        for (chunkX in minChunkX..maxChunkX) {
            for (chunkZ in minChunkZ..maxChunkZ) {
                val chunkCoord = ChunkCoord(world.uid, chunkX, chunkZ)

                if (!cache.computeIfAbsent(chunkCoord) { world.isChunkLoaded(chunkX, chunkZ) }) continue

                val chunk = world.getChunkAt(chunkX, chunkZ)

                for (tileEntity in chunk.tileEntities) {
                    if (tileEntity !is Container) continue

                    val block = tileEntity.block

                    if (!containerTypes.contains(block.type)) continue

                    val dx = block.x - centerX
                    val dz = block.z - centerZ

                    if (dx * dx + dz * dz > radiusSquared) continue
                    if (containerFilter(block)) containers.add(block)
                }
            }
        }

        if (cache.size > 1000) cache.clear()

        return containers
    }

    /**
     * Filter out double chest duplicates and sort by distance.
     * @param containers List of container blocks.
     * @param centerLocation The center location for distance calculation.
     * @return Filtered and sorted list of containers.
     */
    fun filterAndSortContainers(
        containers: List<Block>,
        centerLocation: Location,
    ): List<Block> {
        val seenDoubleChests = mutableSetOf<InventoryHolder?>()
        val filteredContainers =
            containers.filter { block ->
                val inventory = (block.state as Container).inventory
                val holder = inventory.holder
                if (holder is DoubleChest) seenDoubleChests.add(holder.leftSide)
                true
            }

        return filteredContainers.sortedBy { it.location.distanceSquared(centerLocation) }
    }
}
