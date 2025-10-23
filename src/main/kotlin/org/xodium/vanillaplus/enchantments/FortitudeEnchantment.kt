package org.xodium.vanillaplus.enchantments

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlusBootstrap.Companion.FORTITUDE
import org.xodium.vanillaplus.VanillaPlusBootstrap.Companion.INSTANCE
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

@Suppress("UnstableApiUsage")
internal object FortitudeEnchantment : EnchantmentInterface {
    override val key: TypedKey<Enchantment> = TypedKey.create(RegistryKey.ENCHANTMENT, Key.key(INSTANCE, "fortitude"))

    override fun init(builder: EnchantmentRegistryEntry.Builder) =
        builder
            .description(FORTITUDE.value().replaceFirstChar { it.uppercase() }.mm())
            .anvilCost(2)
            .maxLevel(4)
            .weight(5)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(4, 6))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(24, 6))
            .activeSlots(EquipmentSlotGroup.BODY)

    fun effect(level: Int): AttributeModifier =
        AttributeModifier(
            NamespacedKey(INSTANCE, "fortitude_armor"),
            2.0 + (level - 1.0) * 2.0,
            AttributeModifier.Operation.ADD_NUMBER,
        )

    fun ItemStack.fortitude(level: Int) {
        setData(
            DataComponentTypes.ATTRIBUTE_MODIFIERS,
            ItemAttributeModifiers.itemAttributes().addModifier(Attribute.ARMOR, effect(level)),
        )
    }
}
