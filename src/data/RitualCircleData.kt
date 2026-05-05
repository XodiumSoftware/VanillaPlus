package org.xodium.illyriaplus.data

import org.bukkit.Location
import org.bukkit.Material

/**
 * Internal state of a ritual circle.
 *
 * @property center The skull location at the center of the candle pattern.
 * @property candles Map of each candle [Location] to its (count, [Material]) pair.
 * @property isActive Whether trails are running and teleport is enabled.
 * @property activeTaskIds Bukkit scheduler task IDs for particle trail tasks.
 */
internal data class RitualCircleData(
    val center: Location,
    val candles: MutableMap<Pair<Int, Int>, Pair<Int, Material>> = mutableMapOf(),
    var isActive: Boolean = false,
    val activeTaskIds: MutableList<Int> = mutableListOf(),
)
