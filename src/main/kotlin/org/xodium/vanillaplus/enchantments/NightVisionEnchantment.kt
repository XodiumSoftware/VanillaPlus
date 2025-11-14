package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents an object handling night vision enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object NightVisionEnchantment : EnchantmentInterface {
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
