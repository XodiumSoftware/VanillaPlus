package org.xodium.vanillaplus.interfaces

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * Represents a contract for custom potions within the system.
 * Potions are consumable items that provide magical effects, most commonly
 * restoring the player's mana for spell casting.
 */
internal interface PotionInterface {
    /**
     * The unique namespaced key identifier for this potion type.
     * Used for recipe registration and internal identification.
     * @return The namespaced key string (e.g., "vanillaplus:mana_potion").
     */
    val key: String

    /**
     * Creates an instance of this potion as an [ItemStack].
     * @return The configured potion item ready for use or brewing.
     */
    fun createPotion(): ItemStack = ItemStack.of(Material.POTION)

    /**
     * Creates a splash potion variant as an [ItemStack].
     * @return The configured splash potion item.
     */
    fun createSplashPotion(): ItemStack = ItemStack.of(Material.SPLASH_POTION)
}
