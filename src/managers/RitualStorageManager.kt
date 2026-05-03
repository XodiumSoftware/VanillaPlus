package org.xodium.illyriaplus.managers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.bukkit.Location
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.data.RitualData
import java.io.File

/** Manages persistent storage of ritual configurations. */
internal object RitualStorageManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val dataFile = File(instance.dataFolder, "rituals.json")
    private val rituals = mutableListOf<RitualData>()

    /** Loads all saved rituals from disk. */
    fun load() {
        if (!dataFile.exists()) {
            dataFile.parentFile.mkdirs()
            dataFile.writeText("[]")
            return
        }

        val json = dataFile.readText()
        val type = object : TypeToken<List<RitualData>>() {}.type
        val loaded: List<RitualData> = gson.fromJson(json, type) ?: emptyList()

        rituals.clear()
        rituals.addAll(loaded)
    }

    /** Saves all rituals to disk. */
    fun save() {
        dataFile.parentFile.mkdirs()
        dataFile.writeText(gson.toJson(rituals))
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

    /** Returns all stored rituals. */
    fun getAllRituals(): List<RitualData> = rituals.toList()

    /**
     * Checks if a ritual exists at the given location.
     *
     * @param center The center location to check.
     * @return True if a ritual exists at this location.
     */
    fun hasRitualAt(center: Location): Boolean =
        rituals.any {
            it.x == center.blockX &&
                it.y == center.blockY &&
                it.z == center.blockZ &&
                it.world == center.world?.name
        }
}
