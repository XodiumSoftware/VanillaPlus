package org.xodium.illyriaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import org.xodium.illyriaplus.data.KingdomData
import org.xodium.illyriaplus.managers.KingdomManager
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Provides access to [Player]-specific persistent data. */
@OptIn(ExperimentalUuidApi::class)
internal object PlayerPDC {
    /** The [NamespacedKey] used for storing kingdom UUID. */
    private val KINGDOM_KEY = NamespacedKey(instance, "kingdom")

    /**
     * Gets or sets a [Player]'s kingdom in their persistent data container.
     * Stores the UUID in PDC and syncs with [KingdomManager].
     * @return The [Player]'s [KingdomData], or `null` if not in a kingdom.
     */
    var Player.kingdom: KingdomData?
        get() =
            persistentDataContainer
                .get(KINGDOM_KEY, PersistentDataType.STRING)
                ?.let { KingdomManager[Uuid.parse(it)] }
        set(value) {
            if (value == null) {
                persistentDataContainer.remove(KINGDOM_KEY)
            } else {
                persistentDataContainer.set(KINGDOM_KEY, PersistentDataType.STRING, value.id.toString())
                KingdomManager.update(value)
            }
        }
}
