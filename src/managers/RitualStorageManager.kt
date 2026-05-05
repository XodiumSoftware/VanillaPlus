package org.xodium.illyriaplus.managers

import org.bukkit.Location
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.data.RitualLocationData
import org.xodium.illyriaplus.data.RitualPairData
import org.xodium.illyriaplus.pdcs.WorldPDC.ritualPairs

/** Manages persistent storage of ritual pairs via World PDC. */
internal object RitualStorageManager {
    private val pairs = mutableListOf<RitualPairData>()

    /** Loads all saved ritual pairs from World PDC. */
    fun load() {
        pairs.clear()
        instance.server.worlds.forEach { pairs.addAll(it.ritualPairs) }
    }

    /** Saves all ritual pairs to their respective worlds' PDC. */
    fun save() {
        instance.server.worlds.forEach { world ->
            world.ritualPairs = pairs.groupBy { it.source.world }[world.name] ?: emptyList()
        }
    }

    /**
     * Attempts to create a ritual pair.
     *
     * If a ritual with the same candle configuration already exists (and is not already
     * paired with this location), creates a pair linking them.
     *
     * @param location The ritual location to add.
     * @return The created [RitualPairData] if a match was found and paired, null otherwise.
     */
    fun tryCreatePair(location: RitualLocationData): RitualPairData? {
        if (isInPair(location)) return null

        val existing = findUnpairedRitual(location.candles)

        return if (existing != null) {
            if (existing.x == location.x &&
                existing.y == location.y &&
                existing.z == location.z &&
                existing.world == location.world
            ) {
                return null
            }

            val pair = RitualPairData(existing, location)

            pairs.add(pair)
            save()
            pair
        } else {
            null
        }
    }

    /**
     * Removes a ritual pair by either source or destination location.
     *
     * @param center The center location of either ritual in the pair.
     */
    fun removePair(center: Location) {
        pairs.removeIf {
            (
                it.source.x == center.blockX && it.source.y == center.blockY &&
                    it.source.z == center.blockZ && it.source.world == center.world?.name
            ) ||
                (
                    it.destination.x == center.blockX && it.destination.y == center.blockY &&
                        it.destination.z == center.blockZ && it.destination.world == center.world?.name
                )
        }
        save()
    }

    /**
     * Finds the paired ritual location for a given ritual.
     *
     * @param location The ritual location to find the pair for.
     * @return The paired [RitualLocationData], or null if not found.
     */
    fun findPair(location: RitualLocationData): RitualLocationData? =
        pairs
            .find {
                it.source.x == location.x && it.source.y == location.y &&
                    it.source.z == location.z && it.source.world == location.world
            }?.destination
            ?: pairs
                .find {
                    it.destination.x == location.x && it.destination.y == location.y &&
                        it.destination.z == location.z && it.destination.world == location.world
                }?.source

    /**
     * Checks if a ritual location is already part of a pair.
     *
     * @param location The ritual location to check.
     * @return True if the location is already paired.
     */
    fun isInPair(location: RitualLocationData): Boolean =
        pairs.any {
            (
                it.source.x == location.x && it.source.y == location.y &&
                    it.source.z == location.z && it.source.world == location.world
            ) ||
                (
                    it.destination.x == location.x && it.destination.y == location.y &&
                        it.destination.z == location.z && it.destination.world == location.world
                )
        }

    /**
     * Finds an unpaired ritual with matching candle configuration.
     *
     * @param candles The candle configuration to match.
     * @return An unpaired [RitualLocationData] with matching candles, or null.
     */
    private fun findUnpairedRitual(candles: Map<String, Pair<Int, String>>): RitualLocationData? =
        getAllRitualLocations()
            .filter { it.candles == candles }
            .find { loc -> pairs.flatMap { listOf(it.source, it.destination) }.none { it.matches(loc) } }

    /** Returns all ritual locations from all pairs. */
    fun getAllRitualLocations(): List<RitualLocationData> = pairs.flatMap { listOf(it.source, it.destination) }

    /**
     * Checks whether this ritual location shares the same center coordinates and world as another.
     *
     * @param other The ritual location to compare against.
     * @return True if the coordinates and world match.
     */
    private fun RitualLocationData.matches(other: RitualLocationData): Boolean =
        x == other.x && y == other.y && z == other.z && world == other.world
}
