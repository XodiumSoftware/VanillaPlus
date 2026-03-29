package org.xodium.vanillaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.pdcs.PlayerPDC.NICKNAME_KEY
import org.xodium.vanillaplus.pdcs.PlayerPDC.RUNE_SLOTS_KEY
import org.xodium.vanillaplus.pdcs.PlayerPDC.SCOREBOARD_VISIBILITY_KEY

/**
 * Provides access to [Player]-specific persistent data including nicknames and scoreboard preferences.
 * @property NICKNAME_KEY The [NamespacedKey] used for storing nickname data.
 * @property SCOREBOARD_VISIBILITY_KEY The [NamespacedKey] used for storing scoreboard visibility preferences.
 * @property RUNE_SLOTS_KEY The [NamespacedKey] used for storing equipped rune slot data.
 */
internal object PlayerPDC {
    private val NICKNAME_KEY = NamespacedKey(instance, "nickname")
    private val SCOREBOARD_VISIBILITY_KEY = NamespacedKey(instance, "scoreboard_visibility")
    private val RUNE_SLOTS_KEY = NamespacedKey(instance, "rune_slots")

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
     * Gets or sets the list of rune types equipped in the player's rune slots.
     * Each entry is a [RuneType][org.xodium.vanillaplus.modules.RuneModule.RuneType] name or empty string for an empty slot.
     * @return A list of 5 slot values.
     */
    var Player.runeSlots: List<String>
        get() =
            persistentDataContainer
                .getOrDefault(RUNE_SLOTS_KEY, PersistentDataType.STRING, "")
                .split(",")
                .let { parts -> List(5) { i -> parts.getOrElse(i) { "" } } }
        set(value) = persistentDataContainer.set(RUNE_SLOTS_KEY, PersistentDataType.STRING, value.joinToString(","))
}
