/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package org.xodium.vanillaplus.invunloadold

import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.Container
import org.bukkit.util.Vector

class BlockUtils internal constructor(val main: Main?) {
    companion object {
        private val CONTAINER_TYPES: EnumSet<Material?>
        private val CONTAINER_NAMES =
            mutableListOf<String?>("(.*)BARREL$", "(.*)CHEST$", "^SHULKER_BOX$", "^(.*)_SHULKER_BOX$")

        init {
            CONTAINER_TYPES =
                de.jeff_media.InvUnload.EnumUtils.getEnumsFromRegexList(Material::class.java, CONTAINER_NAMES)
        }

        fun findBlocksInRadius(loc: Location, radius: Int): MutableList<Block?> {
            val box: BoundingBox = BoundingBox.of(loc, radius.toDouble(), radius.toDouble(), radius.toDouble())
            //List<BlockVector> blocks = de.jeff_media.jefflib.BlockUtils.getBlocks(loc.getWorld(), box, true, blockData -> isChestLikeBlock(blockData.getMaterial()));
            val chunks: MutableList<Chunk> = com.jeff_media.jefflib.BlockUtils.getChunks(loc.getWorld(), box, true)
            val blocks: MutableList<Block?> = ArrayList<Block?>()
            for (chunk in chunks) {
                for (state in chunk.tileEntities) {
                    if (state is Container && isChestLikeBlock(state.type)) {
                        if (state.location.distanceSquared(loc) <= radius * radius) {
                            // Only chests that can be opened

                            if (Main.getInstance().getConfig()
                                    .getBoolean("ignore-blocked-chests", false)
                            ) {
                                val above = state.block.getRelative(BlockFace.UP)
                                if (state.type == Material.CHEST && above.type.isSolid() && above.type
                                        .isOccluding()
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

        fun findChestsInRadius(loc: Location, radius: Int): MutableList<Block?> {
            // Todo
            return findBlocksInRadius(loc, radius)
        }

        fun isChestLikeBlock(material: Material?): Boolean {
            return CONTAINER_TYPES.contains(material)
        }

        fun doesChestContain(inv: Inventory, item: ItemStack): Boolean {
            val main = Main.getInstance()
            val itemsAdder: ItemsAdderWrapper = main.getItemsAdderWrapper()
            val useItemsAdder = main.getConfig().getBoolean("use-itemsadder")
            for (otherItem in inv.getContents()) {
                if (otherItem == null) continue
                if (otherItem.getType() == item.getType()) {
                    if (!main.getEnchantmentUtils().hasMatchingEnchantments(item, otherItem)) continue

                    if (!useItemsAdder) return true

                    // Item ist NOT ItemsAdder item
                    if (!itemsAdder.isItemsAdderItem(item)) {
                        // Only return true if otherItem also is NOT ItemsAdder item

                        if (itemsAdder.isItemsAdderItem(otherItem)) {
                            continue
                        } else {
                            return true
                        }
                    } else {
                        // But other Item is not
                        if (!itemsAdder.isItemsAdderItem(otherItem)) {
                            continue
                        } else {
                            if (itemsAdder.getItemsAdderName(item).equals(itemsAdder.getItemsAdderName(otherItem))) {
                                return true
                            } else {
                                continue
                            }
                        }
                    }
                }
            }
            return false
        }

        fun sortBlockListByDistance(blocks: MutableList<Block?>, loc: Location) {
            blocks.sort(Comparator { b1: Block?, b2: Block? ->
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
                val doubleChestInv: DoubleChestInventory = doubleChest.getInventory() as DoubleChestInventory
                loc = doubleChestInv.getLeftSide().getLocation().add(doubleChestInv.getRightSide().getLocation())
                    .multiply(0.5)
            }
            loc.add(Vector(0.5, 1.0, 0.5))
            return loc
        }

        fun doesChestContainCount(inv: Inventory, mat: Material?): Int {
            var count = 0
            for (item in inv.getContents()) {
                if (item == null) continue
                if (item.getType() == mat) {
                    count += item.getAmount()
                }
            }
            return count
        }
    }
}
