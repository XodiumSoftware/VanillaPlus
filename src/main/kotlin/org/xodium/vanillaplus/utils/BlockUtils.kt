/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils

import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Slab

/** Block utilities. */
object BlockUtils {
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
     * @param blockFaces The list of [BlockFace] to iterate through.
     * @param clockwise If true, iterates clockwise; otherwise, iterates counter-clockwise.
     * @return The iterated [BlockFace].
     */
    fun BlockFace.iterate(blockFaces: List<BlockFace>, clockwise: Boolean = true): BlockFace {
        return iterateEnum(blockFaces, this, clockwise)
    }

    /**
     * Iterates the given [Slab.Type] to the next type in the list.
     * @param slabTypes The list of [Slab.Type] to iterate through.
     * @param clockwise If true, iterates clockwise; otherwise, iterates counter-clockwise.
     * @return The iterated [Slab.Type].
     */
    fun Slab.Type.iterate(slabTypes: List<Slab.Type>, clockwise: Boolean = true): Slab.Type {
        return iterateEnum(slabTypes, this, clockwise)
    }
}