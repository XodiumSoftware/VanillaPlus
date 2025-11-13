package org.xodium.vanillaplus.pdcs

import org.bukkit.NamespacedKey
import org.bukkit.entity.Animals
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.pdcs.AnimalsPDC.FOOD_SEARCHING_KEY

/**
 * Provides access to animal-specific persistent data including food searching status.
 * @property FOOD_SEARCHING_KEY The namespaced key used for storing food searching status data.
 */
internal object AnimalsPDC {
    private val FOOD_SEARCHING_KEY = NamespacedKey(instance, "food_searching")

    /**
     * Gets or sets the food searching status of this animal in its persistent data container.
     * @receiver The [Animals] whose food searching status to access.
     * @return `true` if the animal is marked as searching for `food`, `false` if not or if the data is not set.
     */
    var Animals.searchingFood: Boolean
        get() = persistentDataContainer.get(FOOD_SEARCHING_KEY, PersistentDataType.BOOLEAN) ?: false
        set(value) {
            persistentDataContainer.set(FOOD_SEARCHING_KEY, PersistentDataType.BOOLEAN, value)
        }
}
