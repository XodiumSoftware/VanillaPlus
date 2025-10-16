package org.xodium.vanillaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.entity.Horse
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Provides access to horse-specific persistent data including sold status. */
internal object HorsePDC {
    private val SOLD_KEY = NamespacedKey(instance, "horse_sold")

    /**
     * Retrieves the sold status of this horse from its persistent data container.
     * @receiver The horse whose sold status to retrieve.
     * @return `true` if the horse is marked as sold, `false` if not, or null if the data is not set.
     */
    fun Horse.sold(): Boolean = persistentDataContainer.get(SOLD_KEY, PersistentDataType.BOOLEAN) ?: false

    /**
     * Sets the sold status of this horse in its persistent data container.
     * @receiver The horse whose sold status to modify.
     * @param boolean The sold state to set (`true` for sold, `false` for not sold).
     */
    fun Horse.sold(boolean: Boolean) = persistentDataContainer.set(SOLD_KEY, PersistentDataType.BOOLEAN, boolean)
}
