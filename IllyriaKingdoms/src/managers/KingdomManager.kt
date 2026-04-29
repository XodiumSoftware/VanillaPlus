package org.xodium.illyriaplus.managers

import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import org.xodium.illyriaplus.data.KingdomData
import org.xodium.illyriaplus.pdcs.PlayerPDC.clearKingdomData
import org.xodium.illyriaplus.pdcs.PlayerPDC.kingdomData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

/** Manages all kingdoms stored on players via PDC. */
@OptIn(ExperimentalUuidApi::class)
internal object KingdomManager {
    /** Returns all kingdoms from all online players. */
    fun getAll(): List<KingdomData> = instance.server.onlinePlayers.mapNotNull { it.kingdomData }

    /**
     * Gets a kingdom by its ID from all online players.
     * @param id The kingdom ID.
     * @return The kingdom data, or null if not found.
     */
    fun get(id: Uuid): KingdomData? =
        instance.server.onlinePlayers
            .firstOrNull { it.kingdomData?.id == id }
            ?.kingdomData

    /**
     * Adds or updates a kingdom on its owner player.
     * @param kingdom The kingdom to add/update.
     */
    fun add(kingdom: KingdomData) {
        instance.server
            .getOfflinePlayer(kingdom.owner.toJavaUuid())
            .player
            ?.let { it.kingdomData = kingdom }
    }

    /**
     * Removes a kingdom by its ID from its owner player.
     * @param id The kingdom ID to remove.
     * @return True if removed, false if not found.
     */
    fun remove(id: Uuid): Boolean =
        instance.server.onlinePlayers
            .firstOrNull { it.kingdomData?.id == id }
            ?.let {
                it.clearKingdomData()
                true
            } ?: false

    /**
     * Removes a member from a kingdom.
     * @param id The kingdom ID.
     * @param member The member UUID to remove.
     * @return The updated kingdom data, or null if kingdom not found.
     */
    fun removeMember(
        id: Uuid,
        member: Uuid,
    ): KingdomData? =
        get(id)?.let { kingdom -> kingdom.copy(members = kingdom.members.filter { it != member }).also { add(it) } }
}
