package org.xodium.illyriaplus.managers

import org.xodium.illyriaplus.data.KingdomData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Manages kingdom data storage and retrieval.
 * Provides in-memory storage for kingdoms with get-or-create semantics.
 */
@OptIn(ExperimentalUuidApi::class)
internal object KingdomManager {
    private val kingdoms = mutableMapOf<Uuid, KingdomData>()

    /**
     * Gets an existing kingdom by ID, or creates a new one if it doesn't exist.
     *
     * @param id The UUID of the kingdom to get or create.
     * @return The existing or newly created [KingdomData].
     */
    fun getOrCreate(id: Uuid): KingdomData =
        kingdoms.getOrPut(id) {
            KingdomData(id = id, name = "<gradient:#FFA751:#FFE259>Kingdom</gradient>", members = emptyList())
        }

    /**
     * Gets an existing kingdom by ID, or null if it doesn't exist.
     *
     * @param id The UUID of the kingdom to retrieve.
     * @return The [KingdomData] if found, null otherwise.
     */
    fun get(id: Uuid): KingdomData? = kingdoms[id]
}
