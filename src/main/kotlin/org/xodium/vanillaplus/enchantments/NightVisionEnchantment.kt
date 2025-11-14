package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.xodium.vanillaplus.VanillaPlusBootstrap.Companion.INSTANCE
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a object handling night vision enchantment implementation within the system. */
internal object NightVisionEnchantment : EnchantmentInterface {
    override val key: TypedKey<Enchantment> =
        TypedKey.create(RegistryKey.ENCHANTMENT, Key.key(INSTANCE, "night_vision"))

    override fun builder(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(key.value().replaceFirstChar { it.uppercase() }.mm())
            .anvilCost(TODO())
            .maxLevel(TODO())
            .weight(TODO())
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(TODO()))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(TODO()))
            .activeSlots(TODO())
}
