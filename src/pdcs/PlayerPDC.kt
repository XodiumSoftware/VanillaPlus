package org.xodium.vanillaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Provides access to [Player]-specific persistent data including nicknames and scoreboard preferences. */
@Suppress("Unused")
internal object PlayerPDC {
    /** The [NamespacedKey] used for storing nickname data. */
    private val NICKNAME_KEY = NamespacedKey(instance, "nickname")

    /** The [NamespacedKey] used for storing scoreboard visibility preferences. */
    private val SCOREBOARD_VISIBILITY_KEY = NamespacedKey(instance, "scoreboard_visibility")

    /** The [NamespacedKey] used for storing mana (shared between Inferno and Glacial). */
    val MANA_KEY = NamespacedKey(instance, "mana")

    /**
     * Gets or sets a [Player]'s nickname in their persistent data container.
     * @return The [Player]'s nickname, or their actual name if no nickname is set.
     */
    var Player.nickname: String
        get() = persistentDataContainer.getOrDefault(NICKNAME_KEY, PersistentDataType.STRING, name)
        set(value) {
            if (value.isBlank()) {
                persistentDataContainer.remove(NICKNAME_KEY)
            } else {
                persistentDataContainer.set(NICKNAME_KEY, PersistentDataType.STRING, value)
            }
        }

    /**
     * Gets or sets a [Player]'s scoreboard visibility preference in their persistent data container.
     * @return `true` if the scoreboard is visible, `false` otherwise.
     */
    var Player.scoreboardVisibility: Boolean
        get() = persistentDataContainer.getOrDefault(SCOREBOARD_VISIBILITY_KEY, PersistentDataType.BOOLEAN, false)
        set(value) = persistentDataContainer.set(SCOREBOARD_VISIBILITY_KEY, PersistentDataType.BOOLEAN, value)

    /**
     * Gets or sets the player's current mana stored in their persistent data container.
     * This pool is shared between Inferno and Glacial enchantments.
     * @return The player's current mana value, defaulting to 100 if unset.
     */
    var Player.mana: Int
        get() = persistentDataContainer.getOrDefault(MANA_KEY, PersistentDataType.INTEGER, 100)
        set(value) = persistentDataContainer.set(MANA_KEY, PersistentDataType.INTEGER, value)
}
