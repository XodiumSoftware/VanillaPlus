/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

/**
 * Data class representing dimension data.
 *
 * @property guiIndex The index of the item in the GUI.
 * @property worldName The name of the world.
 * @property displayName The display name of the dimension.
 * @property itemModelNumber The model number associated with the item.
 */
data class DimensionData(
    val guiIndex: Int,
    val worldName: String,
    val displayName: String,
    val itemModelNumber: Int,
)
