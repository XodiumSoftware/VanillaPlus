/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.managers

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.xodium.vanillaplus.data.SchematicBlockData
import java.io.File


/**
 * Manages loading and pasting schematics.
 */
object SchematicManager {
    val blocks = mutableListOf<SchematicBlockData>()

    /**
     * Load a schematic from a file.
     *
     * @param file the file to load the schematic from
     * @throws IllegalArgumentException if the file is not a valid schematic
     */
    fun load(file: File) {
        // Read entire file into a single string.
        val content = file.readText()

        // ----- Parse Palette -----
        // Look for a Palette section between "Palette:" and the corresponding "}".
        val paletteRegex = Regex("Palette:\\s*\\{(.*?)}", RegexOption.DOT_MATCHES_ALL)
        val paletteMatch = paletteRegex.find(content)
            ?: throw IllegalArgumentException("Palette section not found")
        val paletteContent = paletteMatch.groupValues[1]

        // Build a map from palette index to Material.
        val paletteMap = mutableMapOf<Int, Material>()
        // Each palette entry: "minecraft:acacia_leaves[...]" : <number>
        val paletteEntryRegex = Regex("\"([^\"]+)\"\\s*:\\s*(\\d+)")
        paletteEntryRegex.findAll(paletteContent).forEach { matchResult ->
            val materialStr = matchResult.groupValues[1]
            val index = matchResult.groupValues[2].toInt()
            // Here we remove any "minecraft:" prefix and uppercase the value.
            val cleanMaterial = materialStr.uppercase().replace("MINECRAFT_", "")
            val material = Material.getMaterial(cleanMaterial)
                ?: throw IllegalArgumentException("Invalid material in palette: $materialStr")
            paletteMap[index] = material
        }

        // ----- Parse Data Array -----
        // Locate Data: bytes( ... ) line.
        val dataRegex = Regex("Data:\\s*bytes\\((.*?)\\)", RegexOption.DOT_MATCHES_ALL)
        val dataMatch = dataRegex.find(content)
            ?: throw IllegalArgumentException("Data section not found")
        val dataContent = dataMatch.groupValues[1]
        // Split on commas and parse integers.
        val dataElements = dataContent.split(Regex("\\s*,\\s*")).mapNotNull {
            it.trim().toIntOrNull()
        }
        if (dataElements.isEmpty()) {
            throw IllegalArgumentException("No data found in Data section")
        }

        // ----- Parse Dimensions -----
        // Dimensions are given as e.g. "Width: 16S". We'll extract the number.
        val width = parseDimension(content, "Width")
        val height = parseDimension(content, "Height")
        val length = parseDimension(content, "Length")

        // ----- Parse Offset -----
        // Expected format: "Offset: ints(-6, 0, -6)"
        val offsetRegex = Regex("Offset:\\s*ints\\(([^)]+)\\)")
        val offsetMatch = offsetRegex.find(content)
        val offset = if (offsetMatch != null) {
            val parts = offsetMatch.groupValues[1].split(",").map { it.trim().toInt() }
            if (parts.size != 3) {
                throw IllegalArgumentException("Invalid Offset format")
            }
            Triple(parts[0], parts[1], parts[2])
        } else {
            Triple(0, 0, 0)
        }

        // Clear any previously loaded blocks.
        blocks.clear()

        // Ensure the data size matches the expected dimensions.
        if (dataElements.size != width * height * length) {
            throw IllegalArgumentException("Data size does not match dimensions: expected ${width * height * length} but got ${dataElements.size}")
        }

        // ----- Process the Data -----
        // Data ordering: we assume x changes fastest, then z, then y.
        // Each index in data corresponds to a palette index whose material we look up.
        for (y in 0 until height) {
            for (z in 0 until length) {
                for (x in 0 until width) {
                    val index = x + z * width + y * width * length
                    val paletteIndex = dataElements[index]
                    val material = paletteMap[paletteIndex]
                        ?: throw IllegalArgumentException("No material found for palette index: $paletteIndex")
                    // Calculate the block's relative position, applying the schematic's offset.
                    blocks.add(
                        SchematicBlockData(
                            x + offset.first,
                            y + offset.second,
                            z + offset.third,
                            material
                        )
                    )
                }
            }
        }
    }

    /**
     * Parse a dimension from the schematic content.
     *
     * @param content the schematic content
     * @param key the dimension key to search for
     * @return the parsed dimension
     * @throws IllegalArgumentException if the key is not found
     */
    private fun parseDimension(content: String, key: String): Int {
        return Regex("$key:\\s*(\\d+)", RegexOption.IGNORE_CASE).find(content)?.groupValues?.get(1)?.toInt()
            ?: throw IllegalArgumentException("$key not found in schematic")
    }

    /**
     * Paste the schematic into the world at the given location.
     *
     * @param world the world to paste the schematic into
     * @param location the location to paste the schematic at
     * @param ignoreAirBlocks if true, air blocks in the schematic will be ignored
     * @param ignoreVoidBlocks if true, void blocks in the schematic will be ignored
     */
    fun paste(world: World, location: Location, ignoreAirBlocks: Boolean, ignoreVoidBlocks: Boolean) {
        blocks.forEach {
            when {
                ignoreAirBlocks && it.material == Material.AIR -> return@forEach
                ignoreVoidBlocks && it.material == Material.STRUCTURE_VOID -> return@forEach
                else -> world.getBlockAt(
                    location.clone().add(
                        it.offsetX.toDouble(),
                        it.offsetY.toDouble(),
                        it.offsetZ.toDouble()
                    )
                ).type = it.material
            }
        }
    }
}