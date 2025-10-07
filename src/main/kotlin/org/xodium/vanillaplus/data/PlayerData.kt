@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.data

import org.bukkit.entity.Player
import org.xodium.vanillaplus.interfaces.DataInterface
import java.util.*

/**
 * Represents the data structure for player data.
 * @param nickname The [nickname] of the player, if set.
 * @param scoreboardVisibility The scoreboard to toggle, default: true.
 */
internal data class PlayerData(
    val nickname: String? = null,
    val scoreboardVisibility: Boolean = true,
) {
    companion object : DataInterface<PlayerData> {
        override val dataClass = PlayerData::class
        override val cache = mutableMapOf<UUID, PlayerData>()

        init {
            load()
        }

        /**
         * Retrieves the [PlayerData] for the specified player.
         * @param player The player whose data to retrieve.
         * @return The player's data, or null if no data exists for this player.
         */
        fun get(player: Player): PlayerData? = super.get(player.uniqueId)

        /**
         * Updates the [PlayerData] for the specified player with partial or complete values.
         * @param player The player whose data to update.
         * @param nickname The new nickname for the player. If null, the existing nickname is preserved.
         * @param scoreboardVisibility The new scoreboard visibility state. If null, the existing state is preserved.
         */
        fun set(
            player: Player,
            nickname: String? = null,
            scoreboardVisibility: Boolean? = null,
        ) = super.set(
            player.uniqueId,
            get(player)?.copy(
                nickname = nickname ?: get(player)?.nickname,
                scoreboardVisibility = scoreboardVisibility ?: get(player)?.scoreboardVisibility ?: true,
            ) ?: PlayerData(nickname, scoreboardVisibility ?: true),
        )
    }
}
