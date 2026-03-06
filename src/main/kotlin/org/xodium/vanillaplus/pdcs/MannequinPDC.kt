package org.xodium.vanillaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.entity.Mannequin
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.pdcs.MannequinPDC.OWNER_KEY

/**
 * Provides access to mannequin-specific persistent data, including ownership.
 * @property OWNER_KEY The namespaced key used for storing the mannequin owner.
 */
internal object MannequinPDC {
    private val OWNER_KEY = NamespacedKey(instance, "owner")

    /**
     * Gets or sets the mannequin's owner in its persistent data container.
     * @receiver The mannequin whose owner to access.
     * @return The owner's UUID as a string.
     */
    var Mannequin.owner: String
        get() = persistentDataContainer.get(OWNER_KEY, PersistentDataType.STRING) ?: ""
        set(value) = persistentDataContainer.set(OWNER_KEY, PersistentDataType.STRING, value)
}
