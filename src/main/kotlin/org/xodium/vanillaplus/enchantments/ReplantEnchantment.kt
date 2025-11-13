package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.key.Key
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup
import org.xodium.vanillaplus.VanillaPlusBootstrap.Companion.INSTANCE
import org.xodium.vanillaplus.VanillaPlusBootstrap.Companion.REPLANT
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

@Suppress("UnstableApiUsage")
internal object ReplantEnchantment : EnchantmentInterface {
    override val key: TypedKey<Enchantment> = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key(INSTANCE, "replant"))

    override fun init(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(REPLANT.value().replaceFirstChar { it.uppercase() }.mm())
            // TODO: adjust config values.
            .anvilCost(7)
            .maxLevel(1)
            .weight(1)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 0))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(65, 0))
            .activeSlots(EquipmentSlotGroup.MAINHAND)
}
