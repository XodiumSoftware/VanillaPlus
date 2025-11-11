package org.xodium.vanillaplus.utils

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/** ItemStack utilities. */
internal object ItemStackUtils {
    /**
     * Checks if two ItemStacks have matching enchantments.
     * @param first The first ItemStack.
     * @param second The second ItemStack.
     * @return True if the enchantments match, false otherwise.
     */
    @Suppress("UnstableApiUsage")
    fun hasMatchingEnchantments(
        first: ItemStack,
        second: ItemStack,
    ): Boolean {
        if (first.type != Material.ENCHANTED_BOOK) return true
        // Early return if both items have no enchantments
        if (first.enchantments.isEmpty() && second.enchantments.isEmpty()) return true
        // Gets enchantments from the ItemStack Data.
        val firstEnchants = first.getData(DataComponentTypes.ENCHANTMENTS)
        val secondEnchants = second.getData(DataComponentTypes.ENCHANTMENTS)
        // Gets the stored enchantments from the ItemStack Data.
        val firstStoredEnchants = first.getData(DataComponentTypes.STORED_ENCHANTMENTS)
        val secondStoredEnchants = second.getData(DataComponentTypes.STORED_ENCHANTMENTS)
        // Compares the enchantments and stored enchantments between the 2 ItemStack Data's.
        return firstEnchants == secondEnchants && firstStoredEnchants == secondStoredEnchants
    }
}
