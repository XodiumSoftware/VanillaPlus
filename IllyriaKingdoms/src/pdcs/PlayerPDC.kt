package org.xodium.illyriaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import org.xodium.illyriaplus.data.KingdomData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

/** Provides access to player-specific persistent data for kingdoms. */
@OptIn(ExperimentalUuidApi::class)
internal object PlayerPDC {
    /** The key used for storing the player's kingdom ID. */
    private val KINGDOM_ID_KEY = NamespacedKey(instance, "kingdom_id")

    /** The key used for storing the kingdom name. */
    private val KINGDOM_NAME_KEY = NamespacedKey(instance, "kingdom_name")

    /** The key used for storing the kingdom members list (comma-separated UUIDs). */
    private val KINGDOM_MEMBERS_KEY = NamespacedKey(instance, "kingdom_members")

    /** The key used for storing the kingdom NPCs list (comma-separated UUIDs). */
    private val KINGDOM_NPCS_KEY = NamespacedKey(instance, "kingdom_npcs")

    /**
     * Gets or sets the player's kingdom ID.
     *
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

    /**
     * Gets or sets the player's kingdom data.
     *
     * @return The kingdom data, or null if the player has no kingdom.
     */
    var Player.kingdomData: KingdomData?
        get() {
            val id = persistentDataContainer.get(KINGDOM_ID_KEY, PersistentDataType.STRING) ?: return null
            val name =
                persistentDataContainer.get(KINGDOM_NAME_KEY, PersistentDataType.STRING)
                    ?: "<gradient:#FFA751:#FFE259>Kingdom</gradient>"
            val membersString = persistentDataContainer.get(KINGDOM_MEMBERS_KEY, PersistentDataType.STRING) ?: ""
            val npcsString = persistentDataContainer.get(KINGDOM_NPCS_KEY, PersistentDataType.STRING) ?: ""

            return KingdomData(
                id = Uuid.parse(id),
                owner = uniqueId.toKotlinUuid(),
                name = name,
                members = if (membersString.isEmpty()) emptyList() else membersString.split(",").map { Uuid.parse(it) },
                npcs = if (npcsString.isEmpty()) emptyList() else npcsString.split(",").map { Uuid.parse(it) },
            )
        }
        set(value) {
            persistentDataContainer.let {
                if (value != null) {
                    it.set(KINGDOM_ID_KEY, PersistentDataType.STRING, value.id.toString())
                    it.set(KINGDOM_NAME_KEY, PersistentDataType.STRING, value.name)
                    it.set(
                        KINGDOM_MEMBERS_KEY,
                        PersistentDataType.STRING,
                        value.members.joinToString(",") { uuid -> uuid.toString() },
                    )
                    it.set(
                        KINGDOM_NPCS_KEY,
                        PersistentDataType.STRING,
                        value.npcs.joinToString(",") { uuid -> uuid.toString() },
                    )
                } else {
                    it.remove(KINGDOM_ID_KEY)
                    it.remove(KINGDOM_NAME_KEY)
                    it.remove(KINGDOM_MEMBERS_KEY)
                    it.remove(KINGDOM_NPCS_KEY)
                }
            }
        }

    /**
     * Clears all kingdom-related data from this player.
     */
    fun Player.clearKingdomData() {
        persistentDataContainer.let {
            it.remove(KINGDOM_ID_KEY)
            it.remove(KINGDOM_NAME_KEY)
            it.remove(KINGDOM_MEMBERS_KEY)
            it.remove(KINGDOM_NPCS_KEY)
        }
    }
}
