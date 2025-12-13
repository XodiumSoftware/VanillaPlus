package org.xodium.vanillaplus.recipes

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.RecipeInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents an object handling cursed clock recipe implementation within the system. */
internal object CursedClockRecipe : RecipeInterface {
    private val cursedClockKey = NamespacedKey(instance, "cursed_clock")

    @Suppress("UnstableApiUsage")
    private val cursedClock: ItemStack =
        ItemStack.of(Material.CLOCK).apply {
            setData(DataComponentTypes.CUSTOM_NAME, "Cursed Clock".mm())
            editPersistentDataContainer { container -> container.set(cursedClockKey, PersistentDataType.BYTE, 1) }
        }

    override val recipes =
        setOf(
            ShapelessRecipe(cursedClockKey, cursedClock).apply {
                addIngredient(Material.CLOCK)
                addIngredient(Material.ROTTEN_FLESH)
            },
        )

    /**
     * Checks if an item is a cursed clock.
     * @param item The item to check.
     * @return True if the item is a cursed clock.
     */
    fun isCursedClock(item: ItemStack?): Boolean =
        item?.itemMeta?.persistentDataContainer?.has(cursedClockKey, PersistentDataType.BYTE) == true
}
