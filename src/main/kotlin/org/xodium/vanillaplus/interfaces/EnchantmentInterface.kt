package org.xodium.vanillaplus.interfaces

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.enchantments.Enchantment

/** Represents a contract for enchantments within the system. */
@Suppress("UnstableApiUsage")
internal interface EnchantmentInterface {
    /**
     * The unique typed key that identifies this enchantment in the registry.
     * @see TypedKey
     * @see RegistryKey.ENCHANTMENT
     */
    val key: TypedKey<Enchantment>

    /**
     * Initializes the Drift enchantment.
     * @param builder The builder used to define the enchantment properties.
     * @return The builder for method chaining.
     */
    fun init(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder

    /**
     * Retrieves the enchantment from the registry.
     * @return The [Enchantment] instance corresponding to the key.
     * @throws NoSuchElementException if the enchantment is not found in the registry.
     */
    fun get(): Enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(key)
}
