package org.xodium.illyriaplus.managers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.data.RitualData

/** Manages persistent storage of ritual configurations via World PDC. */
internal object RitualStorageManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val key = NamespacedKey(instance, "illyriaplus_rituals")
    private val rituals = mutableListOf<RitualData>()

    /** Loads all saved rituals from World PDC. */
    fun load() {
        rituals.clear()
        instance.server.worlds.forEach { world ->
            world.persistentDataContainer.get(key, PersistentDataType.STRING)?.let {
                val type = object : TypeToken<List<RitualData>>() {}.type
                val loaded: List<RitualData> = gson.fromJson(it, type) ?: emptyList()

                rituals.addAll(loaded)
            }
        }
    }

    /** Saves all rituals to their respective worlds' PDC. */
    fun save() {
        val byWorld = rituals.groupBy { it.world }

        instance.server.worlds.forEach {
            val worldRituals = byWorld[it.name]

            if (worldRituals.isNullOrEmpty()) {
                it.persistentDataContainer.remove(key)
            } else {
                it.persistentDataContainer.set(key, PersistentDataType.STRING, gson.toJson(worldRituals))
            }
        }
    }

    /**
     * Adds a new ritual to storage.
     *
     * @param ritual The ritual data to store.
     */
    fun addRitual(ritual: RitualData) {
        rituals.removeIf { it.x == ritual.x && it.y == ritual.y && it.z == ritual.z && it.world == ritual.world }
        rituals.add(ritual)
        save()
    }

    /**
     * Removes a ritual from storage.
     *
     * @param center The center location of the ritual to remove.
     */
    fun removeRitual(center: Location) {
        rituals.removeIf {
            it.x == center.blockX &&
                it.y == center.blockY &&
                it.z == center.blockZ &&
                it.world == center.world?.name
        }
        save()
    }

    /**
     * Finds a ritual with matching candle configuration.
     *
     * @param candles The candle configuration to match (count to material name).
     * @return The matching ritual data, or null if no match found.
     */
    fun findMatchingRitual(candles: Map<String, Pair<Int, String>>): RitualData? =
        rituals.find { it.candles == candles }

    /** Returns a snapshot of all stored rituals. */
    fun getAllRituals(): List<RitualData> = rituals.toList()
}
