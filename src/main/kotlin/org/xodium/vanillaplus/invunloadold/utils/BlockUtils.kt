/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold.utils

import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.*
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import org.xodium.vanillaplus.VanillaPlus
import java.util.*

object BlockUtils {
    private val CONTAINER_TYPES: EnumSet<Material?>
    private val CONTAINER_NAMES =
        mutableListOf<String?>("(.*)BARREL$", "(.*)CHEST$", "^SHULKER_BOX$", "^(.*)_SHULKER_BOX$")

    init {
        CONTAINER_TYPES =
            EnumUtils.getEnumsFromRegexList(Material::class.java, CONTAINER_NAMES) //TODO: replace EnumUtils with own.
    }

    private fun findBlocksInRadius(loc: Location, radius: Int): MutableList<Block?> {
        val box: BoundingBox = BoundingBox.of(loc, radius.toDouble(), radius.toDouble(), radius.toDouble())
        val chunks: MutableList<Chunk> =
            com.jeff_media.jefflib.BlockUtils.getChunks(loc.world, box, true) //TODO: replace jeffs BlockUtils with own.
        val blocks: MutableList<Block?> = ArrayList()
        for (chunk in chunks) {
            for (state in chunk.tileEntities) {
                if (state is Container && isChestLikeBlock(state.type)) {
                    if (state.location.distanceSquared(loc) <= radius * radius) {
                        if (VanillaPlus.Companion.instance.config
                                .getBoolean("ignore-blocked-chests", false)
                        ) {
                            val above = state.block.getRelative(BlockFace.UP)
                            if (state.type == Material.CHEST && above.type.isSolid && above.type
                                    .isOccluding
                            ) {
                                continue
                            }
                        }
                        blocks.add(state.block)
                    }
                }
            }
        }
        return blocks
    }

    fun findChestsInRadius(loc: Location, radius: Int): MutableList<Block?> = findBlocksInRadius(loc, radius)

    private fun isChestLikeBlock(material: Material?): Boolean = CONTAINER_TYPES.contains(material)

    fun doesChestContain(inv: Inventory, item: ItemStack): Boolean {
        for (otherItem in inv.contents) {
            if (otherItem == null) continue
            if (otherItem.type == item.type) {
                if (!EnchantmentUtils.hasMatchingEnchantments(item, otherItem)) continue
            }
        }
        return false
    }

    fun sortBlockListByDistance(blocks: MutableList<Block?>, loc: Location) {
        blocks.sortWith(Comparator sort@{ b1: Block?, b2: Block? ->
            if (b1!!.location.distance(loc) > b2!!.location.distance(loc)) {
                return@sort 1
            }
            -1
        })
    }

    fun getCenterOfBlock(block: Block): Location {
        var loc = block.location
        if (block.state is Chest
            && (block.state as Chest).inventory.holder is DoubleChest
        ) {
            val doubleChest: DoubleChest? = (block.state as Chest).inventory.holder as DoubleChest?
            val doubleChestInv: DoubleChestInventory = doubleChest!!.inventory as DoubleChestInventory
            loc = doubleChestInv.rightSide.location?.let { doubleChestInv.leftSide.location?.add(it) }
                ?.multiply(0.5) ?: block.location
        }
        loc.add(Vector(0.5, 1.0, 0.5))
        return loc
    }

    fun doesChestContainCount(inv: Inventory, mat: Material?): Int {
        var count = 0
        for (item in inv.contents) {
            if (item == null) continue
            if (item.type == mat) {
                count += item.amount
            }
        }
        return count
    }
}