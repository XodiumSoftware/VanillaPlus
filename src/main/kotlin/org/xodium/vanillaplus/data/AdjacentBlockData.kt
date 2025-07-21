package org.xodium.vanillaplus.data

import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Door.Hinge

/**
 * This data class stores information about the block's position relative to another block,
 * as well as metadata about its orientation and hinge type (used primarily for doors).
 * @property offsetX The X-axis offset of the adjacent block relative to the original block.
 *                   A positive value indicates the block is farther to the east,
 *                   and a negative value indicates it is farther to the west.
 * @property offsetZ The Z-axis offset of the adjacent block relative to the original block.
 *                   A positive value indicates the block is farther to the south,
 *                   and a negative value indicates it is farther to the north.
 * @property hinge Indicates the [Hinge] position when the block represents a door, useful for determining
 *                 how the door opens (for example, LEFT, RIGHT).
 * @property facing The direction that the adjacent block is facing, represented by the [BlockFace] enum
 *                  (for example, NORTH, EAST, SOUTH, WEST).
 */
internal data class AdjacentBlockData(
    val offsetX: Int,
    val offsetZ: Int,
    private val hinge: Hinge,
    private val facing: BlockFace
)
