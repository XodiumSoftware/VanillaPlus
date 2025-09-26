@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.data

import org.bukkit.entity.Player
import org.xodium.vanillaplus.interfaces.DataInterface
import java.util.*

/**
 * Represents the data structure for player data.
 * @param uuid The UUID of the player.
 * @param nickname The [nickname] of the player, if set.
 */
internal data class PlayerData(
    val uuid: UUID,
    val nickname: String? = null,
) {
    companion object : DataInterface<PlayerData> {
        override val dataClass = PlayerData::class
        override val fileName = "players.json"
        override val cache = mutableMapOf<UUID, PlayerData>()

        init {
            load()
        }

        /**
         * Sets the data for a specific player.
         * @param player The player whose data is to be set.
         * @param nickname The nickname to set (optional).
         */
        fun set(
            player: Player,
            nickname: String? = null,
        ) {
            set(player.uniqueId, PlayerData(player.uniqueId, nickname))
        }

        /**
         * Retrieves the data for a specific player.
         * @param player The player whose data is to be retrieved.
         * @return The PlayerData associated with the player, or null if not found.
         */
        fun get(player: Player): PlayerData? = get(player.uniqueId)
    }
}
