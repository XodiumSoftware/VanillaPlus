package org.xodium.illyriaplus.pdcs

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance

/** Provides access to player-specific persistent data for kingdoms. */
@OptIn(ExperimentalUuidApi::class)
internal object PlayerPDC {
    /** The key used for storing the player's kingdom ID. */
    private val KINGDOM_ID_KEY = NamespacedKey(instance, "kingdom_id")

    /**
     * Gets or sets the player's kingdom ID.
     * @return The kingdom ID UUID, or null if the player is not in a kingdom.
     */
    var Player.kingdomId: Uuid?
        get() = persistentDataContainer.get(KINGDOM_ID_KEY, PersistentDataType.STRING)?.let { Uuid.parse(it) }
        set(value) {
            persistentDataContainer.let {
                if (value != null) {
                    it.set(KINGDOM_ID_KEY, PersistentDataType.STRING, value.toString())
                } else {
                    it.remove(KINGDOM_ID_KEY)
                }
            }
        }
}
