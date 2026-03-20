package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.inventory.EquipmentSlotGroup
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.Utils.displayName

/**
 * Legacy registration — kept only so existing items bearing the old `vanillaplus:vein_mine`
 * key can be detected and migrated to [EarthrendEnchantment] on player join.
 * Remove this file and its bootstrap registration once all items have been converted.
 */
@Suppress("UnstableApiUsage")
internal object VeinMineEnchantment : EnchantmentInterface {
    override fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(key.displayName())
            .anvilCost(2)
            .maxLevel(3)
            .weight(2)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(25, 5))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(75, 10))
            .activeSlots(EquipmentSlotGroup.MAINHAND)
}
