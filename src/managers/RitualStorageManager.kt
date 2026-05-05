package org.xodium.illyriaplus.managers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.data.RitualLocation
import org.xodium.illyriaplus.data.RitualPair

/** Manages persistent storage of ritual pairs via World PDC. */
internal object RitualStorageManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val key = NamespacedKey(instance, "illyriaplus_ritual_pairs")
    private val pairs = mutableListOf<RitualPair>()

    /** Loads all saved ritual pairs from World PDC. */
    fun load() {
        pairs.clear()
        instance.server.worlds.forEach { world ->
            world.persistentDataContainer.get(key, PersistentDataType.STRING)?.let {
                val type = object : TypeToken<List<RitualPair>>() {}.type
                val loaded: List<RitualPair> = gson.fromJson(it, type) ?: emptyList()

                pairs.addAll(loaded)
            }
        }
    }

    /** Saves all ritual pairs to their respective worlds' PDC. */
    fun save() {
        val byWorld = pairs.groupBy { it.source.world }

        instance.server.worlds.forEach {
            val worldPairs = byWorld[it.name]

            if (worldPairs.isNullOrEmpty()) {
                it.persistentDataContainer.remove(key)
            } else {
                it.persistentDataContainer.set(key, PersistentDataType.STRING, gson.toJson(worldPairs))
            }
        }
    }

    /**
     * Attempts to create a ritual pair.
     *
     * If a ritual with the same candle configuration already exists (and is not already
     * paired with this location), creates a pair linking them.
     *
     * @param location The ritual location to add.
     * @return The created [RitualPair] if a match was found and paired, null otherwise.
     */
    fun tryCreatePair(location: RitualLocation): RitualPair? {
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

            val pair = RitualPair(existing, location)

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
     * @return The paired [RitualLocation], or null if not found.
     */
    fun findPair(location: RitualLocation): RitualLocation? =
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
    fun isInPair(location: RitualLocation): Boolean =
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
     * @return An unpaired [RitualLocation] with matching candles, or null.
     */
    private fun findUnpairedRitual(candles: Map<String, Pair<Int, String>>): RitualLocation? {
        val allPairedLocations = pairs.flatMap { listOf(it.source, it.destination) }
        val allLocations = getAllRitualLocations()

        return allLocations
            .filter { it.candles == candles }
            .find { loc -> allPairedLocations.none { it.matches(loc) } }
    }

    /** Returns all ritual locations from all pairs. */
    fun getAllRitualLocations(): List<RitualLocation> = pairs.flatMap { listOf(it.source, it.destination) }

    private fun RitualLocation.matches(other: RitualLocation): Boolean =
        x == other.x && y == other.y && z == other.z && world == other.world
}
