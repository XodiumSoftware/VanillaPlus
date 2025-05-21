/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("unused")

package org.xodium.vanillaplus.utils

import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Slab

/** Block utilities. */
object BlockUtils {
    private val blockFaces = listOf(
        //TODO: only cartesian directions allowed, see what to do about up,bottom
        BlockFace.NORTH,
        BlockFace.EAST,
        BlockFace.SOUTH,
        BlockFace.WEST,
    )

    private val slabTypes = listOf(
        Slab.Type.BOTTOM,
        Slab.Type.TOP,
    )

    /**
     * Iterates the given [Enum] to the next face in the list.
     * @param list The list of [Enum] to iterate through.
     * @param value The current [Enum] value.
     * @param clockwise If true, iterates clockwise; otherwise, iterates counter-clockwise.
     * @return The iterated [Enum].
     */
    private fun <T> iterateEnum(list: List<T>, value: T, clockwise: Boolean): T {
        val idx = list.indexOf(value)
        if (idx == -1) return value
        val next = if (clockwise) (idx + 1) % list.size else (idx - 1 + list.size) % list.size
        return list[next]
    }

    /**
     * Iterates the given [BlockFace] to the next face in the list.
     * @param clockwise If true, iterates clockwise; otherwise, iterates counter-clockwise.
     * @return The iterated [BlockFace].
     */
    fun BlockFace.iterate(clockwise: Boolean = true): BlockFace = iterateEnum(blockFaces, this, clockwise)


    /**
     * Iterates the given [Slab.Type] to the next type in the list.
     * @param clockwise If true, iterates clockwise; otherwise, iterates counter-clockwise.
     * @return The iterated [Slab.Type].
     */
    fun Slab.Type.iterate(clockwise: Boolean = true): Slab.Type = iterateEnum(slabTypes, this, clockwise)

}