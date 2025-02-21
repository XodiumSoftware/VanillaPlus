/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.bukkit.Material

/**
 * Represents a block in a schematic file.
 *
 * @property offsetX The x offset of the block.
 * @property offsetY The y offset of the block.
 * @property offsetZ The z offset of the block.
 * @property material The material of the block.
 */
data class SchematicBlockData(
    val offsetX: Int,
    val offsetY: Int,
    val offsetZ: Int,
    val material: Material
)