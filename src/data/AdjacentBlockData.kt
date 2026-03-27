package org.xodium.vanillaplus.data

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.Door.Hinge

/**
 * Stores information about a block’s position relative to another block,
 * as well as metadata about its orientation and hinge type (used primarily for doors).
 */
internal data class AdjacentBlockData(
    /** The X-axis offset relative to the original block. Positive = east, negative = west. */
    val offsetX: Int,
    /** The Z-axis offset relative to the original block. Positive = south, negative = north. */
    val offsetZ: Int,
    /** The [Hinge] position when the block represents a door (LEFT or RIGHT). */
    val hinge: Hinge,
    /** The direction the adjacent block is facing. */
    val facing: BlockFace,
) {
    /**
     * Checks if this adjacent block data matches the given door's properties.
     * @param door The door to check against
     * @param originalDoor The original door to compare with
     * @param blockType The material type to match
     * @return `true` if this adjacent data matches a valid door pair
     */
    fun matchesDoorPair(
        door: Door,
        originalDoor: Door,
        blockType: Material,
    ): Boolean =
        door.facing == originalDoor.facing &&
            door.hinge != originalDoor.hinge &&
            door.isOpen == originalDoor.isOpen &&
            blockType == door.material

    /**
     * Gets the relative [Block] using the stored offsets.
     * @param block The original [Block] to get relative from.
     * @return The relative [Block].
     */
    fun getRelativeBlock(block: Block): Block = block.getRelative(offsetX, 0, offsetZ)
}
