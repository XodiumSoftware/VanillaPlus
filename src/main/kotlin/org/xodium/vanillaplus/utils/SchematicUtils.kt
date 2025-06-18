/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils

import net.sandrohc.schematic4j.SchematicLoader
import net.sandrohc.schematic4j.schematic.Schematic
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
     * Load a schematic from the given input stream.
     * @param inputStream The schematic file input stream.
     * @return The loaded schematic.
     */
    @Throws(Exception::class)
    fun load(inputStream: InputStream): Schematic = SchematicLoader.load(inputStream)

    /**
     * Paste a schematic at the given block's location.
     * @param origin The origin block.
     * @param schematic The schematic to paste.
     */
    fun paste(origin: Block, schematic: Schematic) {
        (0 until schematic.width()).forEach { x ->
            (0 until schematic.height()).forEach { y ->
                (0 until schematic.length()).forEach { z ->
                    schematic.block(x, y, z).name
                        .takeIf { it != "minecraft:air" }
                        ?.let(Material::matchMaterial)
                        ?.let { origin.world.setBlockAt(origin.x + x, origin.y + y, origin.z + z, it) }
                }
            }
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
