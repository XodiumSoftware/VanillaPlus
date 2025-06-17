/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils

import net.sandrohc.schematic4j.SchematicLoader
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import java.io.InputStream

//TODO: re-implement missing features.
//TODO: fix log spam.
//TODO: add support for block states (e.g., orientation, properties).

/** Utility object for loading and pasting schematic4j-based schematic files. */
object SchematicUtils {
    /**
     * Load a schematic from an input stream using `schematic4j`.
     * Only loads block positions; does not preserve block states.
     * @param inputStream The schematic file input stream.
     * @return A list of block positions relative to the origin.
     */
    @Throws(Exception::class)
    fun load(inputStream: InputStream): List<Triple<Int, Int, Int>> {
        val schematic = SchematicLoader.load(inputStream)
        val width = schematic.width()
        val height = schematic.height()
        val length = schematic.length()
        val blockPositions = mutableListOf<Triple<Int, Int, Int>>()

        for (x in 0 until width) {
            for (y in 0 until height) {
                for (z in 0 until length) {
                    val block = schematic.block(x, y, z)
                    if (block.name != "minecraft:air") {
                        blockPositions += Triple(x, y, z)
                    }
                }
            }
        }
        return blockPositions
    }

    /**
     * Paste a schematic at the given block's location.
     * All blocks are placed as OAK_LOG by default.
     * @param origin The origin block.
     * @param schematic The list of relative block positions.
     */
    fun paste(origin: Block, schematic: List<Triple<Int, Int, Int>>) {
        schematic.forEach { (x, y, z) ->
            origin.world.setBlockAt(
                origin.x + x,
                origin.y + y,
                origin.z + z,
                Material.OAK_LOG
            )
        }
    }

    /**
     * Paste a schematic at the given coordinates in the world.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param z The z-coordinate.
     * @param material The material to use for the blocks.
     */
    private fun World.setBlockAt(x: Int, y: Int, z: Int, material: Material) {
        getBlockAt(x, y, z).type = material
    }
}
