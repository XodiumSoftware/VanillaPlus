package org.xodium.vanillaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.block.ShulkerBox
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Provides access to shulker-specific persistent data including sold status. */
internal object ShulkerPDC {
    private val LOCK_KEY = NamespacedKey(instance, "lock")

    /**
     * Retrieves the usage status of this shulker box from its persistent data container.
     * @receiver The shulker box whose usage status to retrieve.
     * @return `true` if the shulker box is marked as in use, `false` otherwise.
     */
    fun ShulkerBox.lock(): Boolean = persistentDataContainer.get(LOCK_KEY, PersistentDataType.BOOLEAN) ?: false

    /**
     * Sets the usage status of this shulker box in its persistent data container.
     * @receiver The shulker box whose usage status to modify.
     * @param boolean The usage state to set (`true` for in use, `false` for not in use).
     */
    fun ShulkerBox.lock(boolean: Boolean) = persistentDataContainer.set(LOCK_KEY, PersistentDataType.BOOLEAN, boolean)
}
