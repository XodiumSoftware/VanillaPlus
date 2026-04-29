package org.xodium.illyriaplus.managers

import org.xodium.illyriaplus.data.KingdomData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Manages all kingdoms in the plugin. */
@OptIn(ExperimentalUuidApi::class)
internal object KingdomManager {
    private val kingdoms = mutableMapOf<Uuid, KingdomData>()

    /** Returns all registered kingdoms. */
    fun getAll(): List<KingdomData> = kingdoms.values.toList()

    /**
     * Gets a kingdom by its ID.
     * @param id The kingdom ID.
     * @return The kingdom data, or null if not found.
     */
    fun get(id: Uuid): KingdomData? = kingdoms[id]

    /**
     * Adds a new kingdom.
     * @param kingdom The kingdom to add.
     */
    fun add(kingdom: KingdomData) {
        kingdoms[kingdom.id] = kingdom
    }

    /**
     * Removes a kingdom by its ID.
     * @param id The kingdom ID to remove.
     * @return True if removed, false if not found.
     */
    fun remove(id: Uuid): Boolean = kingdoms.remove(id) != null
}
