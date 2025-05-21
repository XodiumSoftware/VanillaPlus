/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils

import org.bukkit.block.BlockFace

/** Block utilities. */
object BlockUtils {
    /** Rotates the given [BlockFace] 90 degrees clockwise. */
    fun BlockFace.rotateY(): BlockFace = when (this) {
        BlockFace.NORTH -> BlockFace.EAST
        BlockFace.EAST -> BlockFace.SOUTH
        BlockFace.SOUTH -> BlockFace.WEST
        BlockFace.WEST -> BlockFace.NORTH
        else -> this
    }
}