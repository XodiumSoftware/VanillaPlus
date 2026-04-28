package org.xodium.illyriaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import org.xodium.illyriaplus.items.SceptreItem

/** Provides access to item-specific persistent data for kingdom tools. */
internal object ItemPDC {
    /** The key used for identifying an item. */
    private val SCEPTRE_KEY = NamespacedKey(instance, "sceptre")

    /** The key used for storing the sceptre mode ("gui" or "triggers"). */
    private val SCEPTRE_MODE_KEY = NamespacedKey(instance, "sceptre_mode")

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
     * Gets or sets the sceptre mode. Defaults to GUI.
     * @return The current SceptreMode.
     */
    var ItemStack.sceptreMode: SceptreItem.SceptreMode
        get() =
            persistentDataContainer
                .get(SCEPTRE_MODE_KEY, PersistentDataType.STRING)
                ?.let { SceptreItem.SceptreMode.valueOf(it) }
                ?: SceptreItem.SceptreMode.GUI
        set(value) {
            editPersistentDataContainer { it.set(SCEPTRE_MODE_KEY, PersistentDataType.STRING, value.name) }
        }
}
