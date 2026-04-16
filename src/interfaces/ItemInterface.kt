package org.xodium.vanillaplus.interfaces

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * Represents a contract for custom items within the system.
 * Items include potions and other consumables that provide magical effects.
 */
internal interface ItemInterface {
    /**
     * Creates a drinkable potion variant as an [ItemStack].
     * @return The configured potion item ready for use or brewing.
     */
    fun potion(): ItemStack = ItemStack.of(Material.POTION)

    /**
     * Creates a splash potion variant as an [ItemStack].
     * @return The configured splash potion item.
     */
    fun splashPotion(): ItemStack = ItemStack.of(Material.SPLASH_POTION)

    /**
     * Creates a lingering potion variant as an [ItemStack].
     * @return The configured lingering potion item.
     */
    fun lingeringPotion(): ItemStack = ItemStack.of(Material.LINGERING_POTION)

    /**
     * Creates a tipped arrow variant as an [ItemStack].
     * @return The configured tipped arrow item.
     */
    fun tippedArrow(): ItemStack = ItemStack.of(Material.TIPPED_ARROW)
}
