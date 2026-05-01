package org.xodium.illyriaplus.interfaces

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.Listener
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.IllyriaPlusBootstrap.Companion.INSTANCE
import org.xodium.illyriaplus.utils.Utils.toRegistryKeyFragment

/** Represents a contract for enchantments within the system. */
@Suppress("UnstableApiUsage")
internal interface EnchantmentInterface : Listener {
    /**
     * The unique typed key identifies this enchantment in the registry.
     *
     * @see TypedKey
     * @see RegistryKey.ENCHANTMENT
     */
    val key: TypedKey<Enchantment>
        get() =
            TypedKey.create(RegistryKey.ENCHANTMENT, Key.key(INSTANCE, javaClass.toRegistryKeyFragment<Enchantment>()))

    /**
     * Configures the properties of the enchantment using the provided builder.
     *
     * @param builder The builder used to define the enchantment properties.
     * @return The builder for method chaining.
     */
    fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder = builder

    /**
     * Retrieves the enchantment from the registry.
     *
     * @return The [Enchantment] instance corresponding to the key.
     * @throws NoSuchElementException if the enchantment is not found in the registry.
     */
    fun get(): Enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(key)

    /**
     * Registers this enchantment's event listeners with the plugin manager.
     * Should be called during plugin enable.
     *
     * @return Time taken to register in milliseconds.
     */
    fun registerEvents(): Long {
        val start = System.currentTimeMillis()
        instance.server.pluginManager.registerEvents(this, instance)
        return System.currentTimeMillis() - start
    }
}
