package org.xodium.vanillaplus.interfaces

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import io.papermc.paper.registry.event.RegistryComposeEvent
import net.kyori.adventure.key.Key
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
     * Configures the Drift enchantment.
     * @param builder The builder used to define the enchantment properties.
     * @param event The registry compose event providing access to tags and registration context.
     */
    fun set(
        builder: EnchantmentRegistryEntry.Builder,
        event: RegistryComposeEvent<Enchantment, EnchantmentRegistryEntry.Builder>,
    )

    /**
     * Retrieves the enchantment from the registry.
     * @param key The unique [Key] identifying the enchantment in the registry.
     * @return The [Enchantment] instance corresponding to the key.
     * @throws NoSuchElementException if the enchantment is not found in the registry.
     */
    fun get(key: TypedKey<Enchantment>): Enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(key)
}
