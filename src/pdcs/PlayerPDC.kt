package org.xodium.vanillaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.pdcs.PlayerPDC.MANA_KEY
import org.xodium.vanillaplus.pdcs.PlayerPDC.NICKNAME_KEY
import org.xodium.vanillaplus.pdcs.PlayerPDC.RUNE_SLOTS_KEY
import org.xodium.vanillaplus.pdcs.PlayerPDC.SCOREBOARD_VISIBILITY_KEY

/**
 * Provides access to [Player]-specific persistent data including nicknames and scoreboard preferences.
 * @property NICKNAME_KEY The [NamespacedKey] used for storing nickname data.
 * @property SCOREBOARD_VISIBILITY_KEY The [NamespacedKey] used for storing scoreboard visibility preferences.
 * @property RUNE_SLOTS_KEY The [NamespacedKey] used for storing equipped rune slot data.
 * @property MANA_KEY The [NamespacedKey] used for storing current mana.
 */
internal object PlayerPDC {
    private val NICKNAME_KEY = NamespacedKey(instance, "nickname")
    private val SCOREBOARD_VISIBILITY_KEY = NamespacedKey(instance, "scoreboard_visibility")
    private val RUNE_SLOTS_KEY = NamespacedKey(instance, "rune_slots")
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
     * @return The player's current mana value, defaulting to 100 if unset.
     */
    var Player.mana: Int
        get() = persistentDataContainer.getOrDefault(MANA_KEY, PersistentDataType.INTEGER, 100)
        set(value) = persistentDataContainer.set(MANA_KEY, PersistentDataType.INTEGER, value)

    /**
     * Gets or sets the list of rune types equipped in the player's rune slots.
     * Each entry is a rune [id][org.xodium.vanillaplus.interfaces.RuneInterface.id] or empty string for an empty slot.
     * @return A list of 5 slot values.
     */
    var Player.runeSlots: List<String>
        get() {
            val raw =
                persistentDataContainer.get(RUNE_SLOTS_KEY, PersistentDataType.STRING)
                    ?: return List(5) { "" }
            val parts = raw.split(",")
            return List(5) { i -> parts.getOrElse(i) { "" } }
        }
        set(value) = persistentDataContainer.set(RUNE_SLOTS_KEY, PersistentDataType.STRING, value.joinToString(","))
}
