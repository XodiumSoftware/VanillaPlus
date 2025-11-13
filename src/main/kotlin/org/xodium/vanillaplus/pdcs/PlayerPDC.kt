package org.xodium.vanillaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.pdcs.PlayerPDC.NICKNAME_KEY
import org.xodium.vanillaplus.pdcs.PlayerPDC.SCOREBOARD_VISIBILITY_KEY

/**
 * Provides access to player-specific persistent data including nicknames and scoreboard preferences.
 * @property NICKNAME_KEY The namespaced key used for storing nickname data.
 * @property SCOREBOARD_VISIBILITY_KEY The namespaced key used for storing scoreboard visibility preferences.
 */
internal object PlayerPDC {
    private val NICKNAME_KEY = NamespacedKey(instance, "nickname")
    private val SCOREBOARD_VISIBILITY_KEY = NamespacedKey(instance, "scoreboard_visibility")

    /**
     * Gets or sets the player's nickname in their persistent data container.
     * @receiver The player whose nickname to access.
     * @return The player's nickname, or null if not set.
     */
    var Player.nickname: String?
        get() = persistentDataContainer.get(NICKNAME_KEY, PersistentDataType.STRING)
        set(value) {
            if (value.isNullOrEmpty()) {
                persistentDataContainer.remove(NICKNAME_KEY)
            } else {
                persistentDataContainer.set(NICKNAME_KEY, PersistentDataType.STRING, value)
            }
        }

    /**
     * Gets or sets the player's scoreboard visibility preference in their persistent data container.
     * @receiver The player whose scoreboard visibility to access.
     * @return True if the scoreboard is visible, false if hidden, or null if not set.
     */
    var Player.scoreboardVisibility: Boolean?
        get() = persistentDataContainer.get(SCOREBOARD_VISIBILITY_KEY, PersistentDataType.BOOLEAN)
        set(value) {
            if (value == null) {
                persistentDataContainer.remove(SCOREBOARD_VISIBILITY_KEY)
            } else {
                persistentDataContainer.set(SCOREBOARD_VISIBILITY_KEY, PersistentDataType.BOOLEAN, value)
            }
        }
}
