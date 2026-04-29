package org.xodium.illyriaplus.managers

import org.xodium.illyriaplus.data.KingdomData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Manages all kingdoms in memory and persists them to JSON.
 * Provides lookup by UUID and by player.
 */
@OptIn(ExperimentalUuidApi::class)
internal object KingdomManager {
    private val configManager = ConfigManager()
    private val kingdoms = mutableMapOf<Uuid, KingdomData>()

    /** All loaded kingdoms mapped by their UUID. */
    val allKingdoms: Map<Uuid, KingdomData> get() = kingdoms.toMap()

    init {
        load()
    }

    /**
     * Gets a kingdom by its UUID.
     * @param id The kingdom UUID.
     * @return The [KingdomData] or null if not found.
     */
    operator fun get(id: Uuid): KingdomData? = kingdoms[id]

    /**
     * Creates a new kingdom and saves it.
     * @param kingdom The kingdom to create.
     */
    fun create(kingdom: KingdomData) {
        kingdoms[kingdom.id] = kingdom
        save()
    }

    /**
     * Updates an existing kingdom and saves changes.
     * @param kingdom The updated kingdom data.
     */
    fun update(kingdom: KingdomData) {
        kingdoms[kingdom.id] = kingdom
        save()
    }

    /**
     * Deletes a kingdom by UUID.
     * @param id The kingdom UUID to delete.
     */
    fun delete(id: Uuid) {
        kingdoms.remove(id)
        save()
    }

    private fun load() {
        val loaded = configManager.load("kingdoms", emptyList<KingdomData>())
        kingdoms.clear()
        loaded.forEach { kingdoms[it.id] = it }
    }

    private fun save() {
        configManager.save("kingdoms", kingdoms.values.toList())
    }
}
