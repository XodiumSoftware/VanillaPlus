@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.data

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
    }
}
