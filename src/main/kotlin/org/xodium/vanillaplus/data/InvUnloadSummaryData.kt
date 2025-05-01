/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block

/** Data class representing summary information for a specific inventory unload operation. */
data class InvUnloadSummaryData(
    val chests: List<Block>,
    val materials: Map<Material, Int>,
    val playerLocation: Location
)
