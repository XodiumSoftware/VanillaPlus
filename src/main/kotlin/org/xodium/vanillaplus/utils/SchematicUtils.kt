/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils

import net.kyori.adventure.nbt.BinaryTagIO
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import java.io.InputStream

/** Utility class for loading and pasting simple schematic files. */
object SchematicUtils {
    /**
     * Load a schematic from an input stream.
     * Only loads block positions; does not support full block state fidelity.
     * @param inputStream The schematic file input stream.
     * @return A list of block positions relative to the origin.
     */
    fun loadSimpleSchematic(inputStream: InputStream): List<Triple<Int, Int, Int>> {
        val root = BinaryTagIO.reader().read(inputStream)
        val width = root.getShort("Width").toInt()
        val height = root.getShort("Height").toInt()
        val length = root.getShort("Length").toInt()
        val blocks = root.getByteArray("BlockData")
        val blockList = mutableListOf<Triple<Int, Int, Int>>()

        for (y in 0 until height) {
            for (z in 0 until length) {
                for (x in 0 until width) {
                    val index = y * width * length + z * width + x
                    val stateId = blocks[index].toInt()
                    if (stateId != 0) {
                        blockList += Triple(x, y, z)
                    }
                }
            }
        }
        return blockList
    }

    /**
     * Paste a schematic at the given block's location.
     * All blocks are placed as OAK_LOG by default.
     * @param origin The origin block.
     * @param schematic The list of relative block positions.
     */
    fun pasteSimpleSchematic(origin: Block, schematic: List<Triple<Int, Int, Int>>) {
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
