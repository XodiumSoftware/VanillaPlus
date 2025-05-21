/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("unused")

package org.xodium.vanillaplus.utils

import org.bukkit.block.BlockFace

/** Block utilities. */
object BlockUtils {
    /**
     * Rotates the given [BlockFace] 90 degrees clockwise around the Y-axis.
     * @param clockwise If true, rotates clockwise; otherwise, rotates counter-clockwise.
     * @return The rotated [BlockFace].
     */
    fun BlockFace.rotateY(clockwise: Boolean = true): BlockFace = when (this) {
        BlockFace.NORTH -> if (clockwise) BlockFace.EAST else BlockFace.WEST
        BlockFace.EAST -> if (clockwise) BlockFace.SOUTH else BlockFace.NORTH
        BlockFace.SOUTH -> if (clockwise) BlockFace.WEST else BlockFace.EAST
        BlockFace.WEST -> if (clockwise) BlockFace.NORTH else BlockFace.SOUTH
        else -> this
    }

    /**
     * Rotates the given [BlockFace] 90 degrees clockwise around the X-axis.
     * @param clockwise If true, rotates clockwise; otherwise, rotates counter-clockwise.
     * @return The rotated [BlockFace].
     */
    fun BlockFace.rotateX(clockwise: Boolean = true): BlockFace = when (this) {
        BlockFace.UP -> if (clockwise) BlockFace.NORTH else BlockFace.SOUTH
        BlockFace.NORTH -> if (clockwise) BlockFace.DOWN else BlockFace.UP
        BlockFace.DOWN -> if (clockwise) BlockFace.SOUTH else BlockFace.NORTH
        BlockFace.SOUTH -> if (clockwise) BlockFace.UP else BlockFace.DOWN
        else -> this
    }

    /**
     * Rotates the given [BlockFace] 90 degrees clockwise around the Z-axis.
     * @param clockwise If true, rotates clockwise; otherwise, rotates counter-clockwise.
     * @return The rotated [BlockFace].
     */
    fun BlockFace.rotateZ(clockwise: Boolean = true): BlockFace = when (this) {
        BlockFace.UP -> if (clockwise) BlockFace.EAST else BlockFace.WEST
        BlockFace.EAST -> if (clockwise) BlockFace.DOWN else BlockFace.UP
        BlockFace.DOWN -> if (clockwise) BlockFace.WEST else BlockFace.EAST
        BlockFace.WEST -> if (clockwise) BlockFace.UP else BlockFace.DOWN
        else -> this
    }
}