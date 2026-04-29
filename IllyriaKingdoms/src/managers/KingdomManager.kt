package org.xodium.illyriaplus.managers

import org.xodium.illyriaplus.data.KingdomData
import org.xodium.illyriaplus.data.KingdomsData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Manages kingdom data storage and retrieval.
 * Provides in-memory storage for kingdoms with get-or-create semantics and JSON persistence.
 */
@OptIn(ExperimentalUuidApi::class)
internal object KingdomManager {
    private val configManager = ConfigManager()
    private val kingdoms = mutableMapOf<Uuid, KingdomData>()

    init {
        load()
    }

    /**
     * Loads kingdoms from disk into memory.
     */
    fun load() {
        val data = configManager.load("kingdoms", KingdomsData())

        kingdoms.clear()
        kingdoms.putAll(data.kingdoms.associateBy { it.id })
    }

    /**
     * Saves all kingdoms to disk.
     */
    fun save() {
        configManager.save("kingdoms", KingdomsData(kingdoms.values.toList()))
    }

    /**
     * Gets an existing kingdom by ID, or creates a new one if it doesn't exist.
     * Automatically saves to disk when creating a new kingdom.
     *
     * @param id The UUID of the kingdom to get or create.
     * @return The existing or newly created [KingdomData].
     */
    fun getOrCreate(id: Uuid): KingdomData = kingdoms.getOrPut(id) { KingdomData(id).also { save() } }

    /**
     * Gets an existing kingdom by ID, or null if it doesn't exist.
     *
     * @param id The UUID of the kingdom to retrieve.
     * @return The [KingdomData] if found, null otherwise.
     */
    fun get(id: Uuid): KingdomData? = kingdoms[id]

    /**
     * Gets all kingdoms.
     *
     * @return A list of all [KingdomData].
     */
    fun getAll(): List<KingdomData> = kingdoms.values.toList()

    /**
     * Removes a kingdom by ID and saves to disk.
     *
     * @param id The UUID of the kingdom to remove.
     */
    fun remove(id: Uuid) {
        kingdoms.remove(id)
        save()
    }
}
