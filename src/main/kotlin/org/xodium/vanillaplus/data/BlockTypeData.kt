/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.bukkit.Material

/**
 * Data class to hold information about block types.
 *
 * @property lastMat The last material used.
 * @property valid Indicates if the material is valid.
 */
data class BlockTypeData(
    var lastMat: Material? = null,
    var valid: Boolean = false,
) {
    /**
     * Validates and updates the cache with the given material.
     */
    fun validate(material: Material) {
        lastMat = material
        valid = true
    }
}
