/*
 * Copyright (c) 2025. Xodium.
 * All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Door.Hinge


/**
 * Represents data about a block that is adjacent to a specific block in a Minecraft world.
 * This data class stores information about the block's position relative to another block,
 * as well as metadata about its orientation and hinge type (used primarily for doors).
 *
 * @property offsetX The X-axis offset of the adjacent block relative to the original block.
 *                   A positive value indicates the block is farther to the east,
 *                   and a negative value indicates it is farther to the west.
 * @property offsetZ The Z-axis offset of the adjacent block relative to the original block.
 *                   A positive value indicates the block is farther to the south,
 *                   and a negative value indicates it is farther to the north.
 * @property hinge Indicates the hinge position when the block represents a door, useful for determining
 *                 how the door opens (e.g., LEFT, RIGHT).
 * @property facing The direction that the adjacent block is facing, represented by the `BlockFace` enum
 *                  (e.g., NORTH, EAST, SOUTH, WEST).
 */
data class AdjacentBlockData(
    val offsetX: Int,
    val offsetZ: Int,
    val hinge: Hinge,
    val facing: BlockFace
)
