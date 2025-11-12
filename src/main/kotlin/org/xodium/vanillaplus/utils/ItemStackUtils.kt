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
        if (first.type != Material.ENCHANTED_BOOK || (first.enchantments.isEmpty() && second.enchantments.isEmpty())) return true
        return first.getData(DataComponentTypes.ENCHANTMENTS) == second.getData(DataComponentTypes.ENCHANTMENTS)
    }
}
