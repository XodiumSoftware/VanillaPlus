package org.xodium.vanillaplus.interfaces

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.xodium.vanillaplus.VanillaPlusBootstrap.Companion.INSTANCE

/** Represents a contract for enchantments within the system. */
@Suppress("UnstableApiUsage")
internal interface EnchantmentInterface {
    /**
     * The unique typed key identifies this enchantment in the registry.
     * @see TypedKey
     * @see RegistryKey.ENCHANTMENT
     */
    val key: TypedKey<Enchantment>
        get() =
            TypedKey.create(
                RegistryKey.ENCHANTMENT,
                Key.key(
                    INSTANCE,
                    this::class
                        .simpleName
                        ?.removeSuffix("Enchantment")
                        ?.split(Regex("(?=[A-Z])"))
                        ?.filter { it.isNotEmpty() }
                        ?.joinToString("_") { it.lowercase() }
                        .toString(),
                ),
            )

    /**
     * Configures the properties of the enchantment using the provided builder.
     * @param invoke The builder used to define the enchantment properties.
     * @return The builder for method chaining.
     */
    fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder

    /**
     * Retrieves the enchantment from the registry.
     * @return The [Enchantment] instance corresponding to the key.
     * @throws NoSuchElementException if the enchantment is not found in the registry.
     */
    fun get(): Enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(key)
}
