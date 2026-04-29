package org.xodium.illyriaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import org.xodium.illyriaplus.data.KingdomData
import org.xodium.illyriaplus.managers.KingdomManager
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Provides access to item-specific persistent data for kingdom tools. */
@OptIn(ExperimentalUuidApi::class)
internal object ItemPDC {
    /** The key used for identifying an item. */
    private val SCEPTRE_KEY = NamespacedKey(instance, "sceptre")

    /** The key used for storing the kingdom UUID on a sceptre. */
    private val KINGDOM_ID_KEY = NamespacedKey(instance, "kingdom_id")

    /**
     * Gets or sets whether this item is a sceptre.
     * @return True if this item is a sceptre, false otherwise.
     */
    var ItemStack.isSceptre: Boolean
        get() = persistentDataContainer.get(SCEPTRE_KEY, PersistentDataType.BOOLEAN) ?: false
        set(value) {
            editPersistentDataContainer {
                if (value) it.set(SCEPTRE_KEY, PersistentDataType.BOOLEAN, true) else it.remove(SCEPTRE_KEY)
            }
        }

    /**
     * Gets or sets the kingdom ID associated with this sceptre.
     * @return The [KingdomData] this sceptre belongs to, or null if not set.
     */
    var ItemStack.kingdomId: KingdomData?
        get() =
            persistentDataContainer
                .get(KINGDOM_ID_KEY, PersistentDataType.STRING)
                ?.let { KingdomManager[Uuid.parse(it)] }
        set(value) {
            editPersistentDataContainer {
                if (value == null) {
                    it.remove(KINGDOM_ID_KEY)
                } else {
                    it.set(KINGDOM_ID_KEY, PersistentDataType.STRING, value.id.toString())
                }
            }
        }
}
