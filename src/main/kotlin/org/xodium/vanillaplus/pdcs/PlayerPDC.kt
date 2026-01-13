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
    private val QUESTS_KEY = NamespacedKey(instance, "quests")

    /**
     * Gets or sets the player's nickname in their persistent data container.
     * @receiver The player whose nickname to access.
     * @return The player's nickname, or their actual name if no nickname is set.
     */
    var Player.nickname: String
        get() = persistentDataContainer.get(NICKNAME_KEY, PersistentDataType.STRING) ?: name
        set(value) {
            if (value.isBlank()) {
                persistentDataContainer.remove(NICKNAME_KEY)
            } else {
                persistentDataContainer.set(NICKNAME_KEY, PersistentDataType.STRING, value)
            }
        }

    /**
     * Gets or sets the player's scoreboard visibility preference in their persistent data container.
     * @receiver The player whose scoreboard visibility to access.
     * @return True if the scoreboard is visible, false otherwise.
     */
    var Player.scoreboardVisibility: Boolean
        get() = persistentDataContainer.get(SCOREBOARD_VISIBILITY_KEY, PersistentDataType.BOOLEAN) ?: false
        set(value) = persistentDataContainer.set(SCOREBOARD_VISIBILITY_KEY, PersistentDataType.BOOLEAN, value)

    /**
     * Gets or sets the player's quests in their persistent data container.
     * @receiver The player whose quests to access.
     * @return A string representing the player's quests, or null if not set.
     */
    var Player.quests: String?
        get() = persistentDataContainer.get(QUESTS_KEY, PersistentDataType.STRING)
        set(value) {
            if (value.isNullOrEmpty()) {
                persistentDataContainer.remove(QUESTS_KEY)
            } else {
                persistentDataContainer.set(QUESTS_KEY, PersistentDataType.STRING, value)
            }
        }
}
