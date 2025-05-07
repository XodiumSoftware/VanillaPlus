/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils.invunload

import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.*
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import org.xodium.vanillaplus.registries.MaterialRegistry
import org.xodium.vanillaplus.utils.Utils

//TODO: Move to a more generic location.
object BlockUtils {
    /**
     * Find all blocks in a given radius from a location.
     * @param loc The location to search from.
     * @param radius The radius to search within.
     * @return A list of blocks found within the radius.
     */
    fun findBlocksInRadius(loc: Location, radius: Int): MutableList<Block> {
        val box = BoundingBox.of(loc, radius.toDouble(), radius.toDouble(), radius.toDouble())
        val chunks = getChunksInBox(loc.world, box)
        val radiusSq = radius * radius
        return chunks.flatMap { chunk ->
            chunk.tileEntities.filter { state ->
                state is Container &&
                        MaterialRegistry.CONTAINER_TYPES.contains(state.type) &&
                        state.location.distanceSquared(loc) <= radiusSq &&
                        (state.type != Material.CHEST ||
                                !(state.block.getRelative(BlockFace.UP).type.isSolid &&
                                        state.block.getRelative(BlockFace.UP).type.isOccluding))
            }.map { (it as Container).block }
        }.toMutableList()
    }

    /**
     * Check if a chest contains an item with matching enchantments.
     * @param inv The inventory to check.
     * @param item The item to check for.
     * @return True if the chest contains the item, false otherwise.
     */
    fun doesChestContain(inv: Inventory, item: ItemStack): Boolean {
        return inv.contents.any { otherItem ->
            otherItem != null
                    && otherItem.type == item.type
                    && Utils.hasMatchingEnchantments(item, otherItem)
        }
    }

    /**
     * Sort a list of blocks by their distance to a given location.
     * @param blocks The blocks to check.
     * @param loc The location to sort by.
     */
    fun sortBlockListByDistance(blocks: MutableList<Block>, loc: Location) {
        blocks.sortBy { it.location.distance(loc) }
    }

    /**
     * Get the center of a block.
     * @param block The block to get the center of.
     * @return The center location of the block.
     */
    fun getCenterOfBlock(block: Block): Location {
        val baseLoc = block.location.clone()
        val state = block.state
        val centerLoc = if (state is Chest && state.inventory.holder is DoubleChest) {
            val doubleChest = state.inventory.holder as? DoubleChest
            val left = (doubleChest?.leftSide as? Chest)?.block?.location
            val right = (doubleChest?.rightSide as? Chest)?.block?.location
            if (left != null && right != null) {
                left.clone().add(right).multiply(0.5)
            } else {
                baseLoc
            }
        } else {
            baseLoc
        }
        centerLoc.add(Vector(0.5, 1.0, 0.5))
        return centerLoc
    }

    /**
     * Get the amount of a specific material in a chest.
     * @param inv The inventory to check.
     * @param mat The material to count.
     * @return The amount of the material in the chest.
     */
    fun doesChestContainCount(inv: Inventory, mat: Material?): Int {
        if (mat == null) return 0
        return inv.contents.filter { it?.type == mat }.sumOf { it?.amount ?: 0 }
    }

    /**
     * Get all chunks in a bounding box.
     * @param world The world to get chunks from.
     * @param box The bounding box to get chunks from.
     * @return A list of chunks in the bounding box.
     */
    private fun getChunksInBox(world: World, box: BoundingBox): List<Chunk> {
        val minChunkX = Math.floorDiv(box.minX.toInt(), 16)
        val maxChunkX = Math.floorDiv(box.maxX.toInt(), 16)
        val minChunkZ = Math.floorDiv(box.minZ.toInt(), 16)
        val maxChunkZ = Math.floorDiv(box.maxZ.toInt(), 16)
        return mutableListOf<Chunk>().apply {
            for (x in minChunkX..maxChunkX) {
                for (z in minChunkZ..maxChunkZ) {
                    if (world.isChunkLoaded(x, z)) {
                        add(world.getChunkAt(x, z))
                    }
                }
            }
        }
    }
}