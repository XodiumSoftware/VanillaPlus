package org.xodium.vanillaplus.interfaces

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.xodium.vanillaplus.VanillaPlusBootstrap.Companion.INSTANCE
import org.xodium.vanillaplus.data.ConfigData

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
                    javaClass
                        .simpleName
                        .removeSuffix("Enchantment")
                        .split(Regex("(?=[A-Z])"))
                        .filter { it.isNotEmpty() }
                        .joinToString("_") { it.lowercase() },
                ),
            )

    /**
     * Retrieves the configuration data associated with the module.
     * @return A [ConfigData] object representing the configuration for the module.
     */
    val config: ConfigData
        get() = ConfigData()

    /**
     * Configures the properties of the enchantment using the provided builder.
     * @param invoke The builder used to define the enchantment properties.
     * @return The builder for method chaining.
     */
    fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder = builder

    /**
     * Retrieves the enchantment from the registry.
     * @return The [Enchantment] instance corresponding to the key.
     * @throws NoSuchElementException if the enchantment is not found in the registry.
     */
    fun get(): Enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(key)
}
