package org.xodium.illyriaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Provides access to item-specific persistent data for kingdom tools. */
@OptIn(ExperimentalUuidApi::class)
internal object ItemPDC {
    /** The key used for identifying an item. */
    private val SCEPTRE_KEY = NamespacedKey(instance, "sceptre")

    /** The key used for storing the kingdom ID. */
    private val KINGDOM_KEY = NamespacedKey(instance, "kingdom")

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
     * Gets or sets the kingdom ID associated with this item.
     * @return The kingdom UUID, or a new random UUID if none is set.
     */
    var ItemStack.kingdomId: Uuid
        get() =
            persistentDataContainer.get(KINGDOM_KEY, PersistentDataType.STRING)?.let { Uuid.parse(it) } ?: Uuid.random()
        set(value) {
            editPersistentDataContainer { it.set(KINGDOM_KEY, PersistentDataType.STRING, value.toString()) }
        }
}
