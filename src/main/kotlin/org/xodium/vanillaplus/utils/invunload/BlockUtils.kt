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
import java.util.*

object BlockUtils {
    private val CONTAINER_TYPES: EnumSet<Material>
    private val CONTAINER_NAME_PATTERNS = listOf(
        Regex("(.*)BARREL$"),
        Regex("(.*)CHEST$"),
        Regex("^SHULKER_BOX$"),
        Regex("^(.*)_SHULKER_BOX$")
    )

    init {
        CONTAINER_TYPES = EnumUtils.getEnumsFromRegexList(Material::class.java, CONTAINER_NAME_PATTERNS)
    }

    private fun findBlocksInRadius(loc: Location, radius: Int): MutableList<Block> {
        val box = BoundingBox.of(loc, radius.toDouble(), radius.toDouble(), radius.toDouble())
        val chunks = getChunksInBox(loc.world, box)
        val radiusSq = radius * radius
        return chunks.flatMap { chunk ->
            chunk.tileEntities.filter { state ->
                state is Container &&
                        isChestLikeBlock(state.type) &&
                        state.location.distanceSquared(loc) <= radiusSq &&
                        (state.type != Material.CHEST ||
                                !(state.block.getRelative(BlockFace.UP).type.isSolid &&
                                        state.block.getRelative(BlockFace.UP).type.isOccluding))
            }.map { (it as Container).block }
        }.toMutableList()
    }

    fun findChestsInRadius(loc: Location, radius: Int): MutableList<Block> = findBlocksInRadius(loc, radius)

    private fun isChestLikeBlock(material: Material?): Boolean = CONTAINER_TYPES.contains(material)

    fun doesChestContain(inv: Inventory, item: ItemStack): Boolean {
        return inv.contents.any { otherItem ->
            otherItem != null
                    && otherItem.type == item.type
                    && EnchantmentUtils.hasMatchingEnchantments(item, otherItem)
        }
    }

    fun sortBlockListByDistance(blocks: MutableList<Block>, loc: Location) {
        blocks.sortBy { it.location.distance(loc) }
    }

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

    fun doesChestContainCount(inv: Inventory, mat: Material?): Int {
        if (mat == null) return 0
        return inv.contents.filter { it?.type == mat }.sumOf { it?.amount ?: 0 }
    }

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