package org.xodium.vanillaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Provides access to player-specific persistent data including nicknames and scoreboard preferences. */
internal object PlayerPDC {
    private val NICKNAME_KEY = NamespacedKey(instance, "nickname")
    private val SCOREBOARD_VISIBILITY_KEY = NamespacedKey(instance, "scoreboard_visibility")

    /**
     * Retrieves the player's nickname from their persistent data container.
     * @receiver The player whose nickname to retrieve.
     * @return The player's nickname, or null if no nickname is set.
     */
    fun Player.nickname(): String? = persistentDataContainer.get(NICKNAME_KEY, PersistentDataType.STRING)

    /**
     * Sets or removes the player's nickname in their persistent data container.
     * @receiver The player whose nickname to modify.
     * @param name The nickname to set, or null/empty to remove the current nickname.
     */
    fun Player.nickname(name: String?) {
        if (name.isNullOrEmpty()) {
            persistentDataContainer.remove(NICKNAME_KEY)
        } else {
            persistentDataContainer.set(NICKNAME_KEY, PersistentDataType.STRING, name)
        }
    }

    /**
     * Retrieves the player's scoreboard visibility setting from their persistent data container.
     * @receiver The player whose scoreboard visibility to check.
     * @return The scoreboard visibility state, or null if not set.
     */
    fun Player.scoreboardVisibility(): Boolean? = persistentDataContainer.get(SCOREBOARD_VISIBILITY_KEY, PersistentDataType.BOOLEAN)

    /**
     * Sets the player's scoreboard visibility in their persistent data container.
     * @receiver The player whose scoreboard visibility to modify.
     * @param visible The visibility state to set (true for visible, false for hidden).
     */
    fun Player.scoreboardVisibility(visible: Boolean) {
        persistentDataContainer.set(SCOREBOARD_VISIBILITY_KEY, PersistentDataType.BOOLEAN, visible)
    }
}
