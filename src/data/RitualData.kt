package org.xodium.illyriaplus.data

import org.bukkit.Location
import org.bukkit.Material
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance

/**
 * Represents a ritual location with its candle configuration.
 *
 * @property world World name.
 * @property x X coordinate of center.
 * @property y Y coordinate of center.
 * @property z Z coordinate of center.
 * @property candles Map of relative positions to candle config (count to material).
 */
data class RitualLocation(
    val world: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val candles: Map<String, Pair<Int, String>>,
) {
    /** Returns the center location for the given world. */
    fun getCenter(): Location = Location(instance.server.getWorld(world), x.toDouble(), y.toDouble(), z.toDouble())

    companion object {
        /** Creates a RitualLocation from a location and candle configuration. */
        fun fromLocation(
            center: Location,
            candles: Map<Location, Pair<Int, Material>>,
        ): RitualLocation =
            RitualLocation(
                center.world?.name ?: "",
                center.blockX,
                center.blockY,
                center.blockZ,
                candles
                    .mapKeys { (loc, _) ->
                        val dx = loc.blockX - center.blockX
                        val dy = loc.blockY - center.blockY
                        val dz = loc.blockZ - center.blockZ
                        "$dx,$dy,$dz"
                    }.mapValues { (_, pair) -> pair.first to pair.second.name },
            )
    }
}

/**
 * Represents a pair of linked rituals (source and destination).
 *
 * @property source The source ritual location.
 * @property destination The destination ritual location.
 */
data class RitualPair(
    val source: RitualLocation,
    val destination: RitualLocation,
)
