package org.xodium.illyriaplus.pdcs

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.persistence.PersistentDataType
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.data.RitualPairData

/** Provides access to [World]-specific persistent data for ritual pair storage. */
@Suppress("Unused")
internal object WorldPDC {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val RITUAL_PAIRS_KEY = NamespacedKey(instance, "illyriaplus_ritual_pairs")

    /**
     * Gets or sets the list of [RitualPair] stored in this world's persistent data container.
     *
     * @return The list of ritual pairs, or an empty list if none are stored.
     */
    var World.ritualPairs: List<RitualPairData>
        get() {
            val json = persistentDataContainer.get(RITUAL_PAIRS_KEY, PersistentDataType.STRING) ?: return emptyList()
            val type = object : TypeToken<List<RitualPairData>>() {}.type

            return gson.fromJson(json, type) ?: emptyList()
        }
        set(value) {
            if (value.isEmpty()) {
                persistentDataContainer.remove(RITUAL_PAIRS_KEY)
            } else {
                persistentDataContainer.set(RITUAL_PAIRS_KEY, PersistentDataType.STRING, gson.toJson(value))
            }
        }
}
