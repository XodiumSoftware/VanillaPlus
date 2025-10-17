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
     * Retrieves the food searching status of this animal from its persistent data container.
     * @receiver The [Animals] whose food searching status to retrieve.
     * @return `true` if the animal is marked as searching for food, `false` if not or if the data is not set.
     */
    fun Animals.searchedFood(): Boolean = persistentDataContainer.get(FOOD_SEARCHING_KEY, PersistentDataType.BOOLEAN) ?: false

    /**
     * Sets the food searching status of this animal in its persistent data container.
     * @receiver The [Animals] whose food searching status to modify.
     * @param boolean The food searching state to set (`true` for searching, `false` for not searching).
     */
    fun Animals.searchedFood(boolean: Boolean) = persistentDataContainer.set(FOOD_SEARCHING_KEY, PersistentDataType.BOOLEAN, boolean)
}
