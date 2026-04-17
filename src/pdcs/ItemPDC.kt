package org.xodium.vanillaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Provides access to item-specific persistent data for spell storage on wands. */
@Suppress("Unused")
internal object ItemPDC {
    /** The key used for storing the currently selected spell ID on a wand. */
    private val SELECTED_SPELL_KEY = NamespacedKey(instance, "selected_spell")

    /** The key used for marking an item as a mana potion. */
    private val MANA_POTION_KEY = NamespacedKey(instance, "mana_potion")

    /**
     * Gets or sets the selected spell ID on an item.
     * @return The spell ID string, or empty string if none selected.
     */
    var ItemStack.selectedSpell: String
        get() = persistentDataContainer.get(SELECTED_SPELL_KEY, PersistentDataType.STRING) ?: ""
        set(value) {
            editPersistentDataContainer { it.set(SELECTED_SPELL_KEY, PersistentDataType.STRING, value) }
        }

    /**
     * Gets or sets whether this item is a mana potion.
     * When consumed, mana potions refill the player's mana pool.
     * @return `true` if this item is a mana potion, `false` otherwise.
     */
    var ItemStack.isManaPotion: Boolean
        get() = persistentDataContainer.get(MANA_POTION_KEY, PersistentDataType.BOOLEAN) ?: false
        set(value) {
            editPersistentDataContainer { it.set(MANA_POTION_KEY, PersistentDataType.BOOLEAN, value) }
        }
}
