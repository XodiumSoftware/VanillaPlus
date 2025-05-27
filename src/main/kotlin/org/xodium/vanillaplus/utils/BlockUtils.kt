/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils

import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.MultipleFacing
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs

/** Block utilities. */
object BlockUtils {
    /**
     * Iterates through a list of items, returning the next item based on the current value and direction.
     * @param list The list of items to iterate through.
     * @param value The current item to find the next item for.
     * @param clockwise If true, iterates clockwise; otherwise, iterates counter-clockwise.
     * @return The next item in the list.
     */
    private fun <T> iterator(list: List<T>, value: T, clockwise: Boolean): T {
        val idx = list.indexOf(value)
        if (idx == -1) return value
        val next = if (clockwise) (idx + 1) % list.size else (idx - 1 + list.size) % list.size
        return list[next]
    }

    /**
     * Iterates the given [Bisected.Half] to the next half in the list.
     * @param bisectedHalves The list of [Bisected.Half] to iterate through.
     * @param clockwise If true, iterates clockwise; otherwise, iterates counter-clockwise.
     * @return The iterated [Bisected.Half].
     */
    fun Bisected.Half.iterate(bisectedHalves: List<Bisected.Half>, clockwise: Boolean = true): Bisected.Half {
        return iterator(bisectedHalves, this, clockwise)
    }

    /**
     * Iterates the given [BlockFace] to the next face in the list.
     * @param blockFaces The list of [BlockFace] to iterate through.
     * @param clockwise If true, iterates clockwise; otherwise, iterates counter-clockwise.
     * @return The iterated [BlockFace].
     */
    fun BlockFace.iterate(blockFaces: List<BlockFace>, clockwise: Boolean = true): BlockFace {
        return iterator(blockFaces, this, clockwise)
    }

    /**
     * Iterates the given [Slab.Type] to the next type in the list.
     * @param slabTypes The list of [Slab.Type] to iterate through.
     * @param clockwise If true, iterates clockwise; otherwise, iterates counter-clockwise.
     * @return The iterated [Slab.Type].
     */
    fun Slab.Type.iterate(slabTypes: List<Slab.Type>, clockwise: Boolean = true): Slab.Type {
        return iterator(slabTypes, this, clockwise)
    }

    /**
     * Iterates the given [Stairs.Shape] to the next shape in the list.
     * @param stairsShapes The list of [Stairs.Shape] to iterate through.
     * @param clockwise If true, iterates clockwise; otherwise, iterates counter-clockwise.
     * @return The iterated [Stairs.Shape].
     */
    fun Stairs.Shape.iterate(stairsShapes: List<Stairs.Shape>, clockwise: Boolean = true): Stairs.Shape {
        return iterator(stairsShapes, this, clockwise)
    }

    /**
     * Iterates the faces of a [MultipleFacing] block data.
     * @param faces The list of [BlockFace] to iterate through.
     * @param clockwise If true, iterates clockwise; otherwise, iterates counter-clockwise.
     */
    fun MultipleFacing.iterate(faces: List<BlockFace>, clockwise: Boolean = true) {
        val current = faces.firstOrNull { hasFace(it) } ?: faces.first()
        val next = iterator(faces, current, clockwise)
        setFace(next, !hasFace(next))
    }
}