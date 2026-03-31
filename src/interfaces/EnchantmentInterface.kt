package org.xodium.vanillaplus.interfaces

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.Event
import org.xodium.vanillaplus.VanillaPlusBootstrap.Companion.INSTANCE
import org.xodium.vanillaplus.utils.Utils.toRegistryKeyFragment

/** Represents a contract for enchantments within the system. */
@Suppress("UnstableApiUsage")
internal interface EnchantmentInterface<T : Event> {
    /**
     * The unique typed key identifies this enchantment in the registry.
     * @see TypedKey
     * @see RegistryKey.ENCHANTMENT
     */
    val key: TypedKey<Enchantment>
        get() =
            TypedKey.create(RegistryKey.ENCHANTMENT, Key.key(INSTANCE, javaClass.toRegistryKeyFragment<Enchantment>()))

    /**
     * Configures the properties of the enchantment using the provided builder.
     * @param invoke The builder used to define the enchantment properties.
     * @return The builder for method chaining.
     */
    fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder = builder

    /**
     * Applies the enchantment's effect in response to the triggering event.
     * @param event The event that triggered the enchantment effect.
     */
    fun effect(event: T)

    /**
     * Retrieves the enchantment from the registry.
     * @return The [Enchantment] instance corresponding to the key.
     * @throws NoSuchElementException if the enchantment is not found in the registry.
     */
    fun get(): Enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(key)
}
