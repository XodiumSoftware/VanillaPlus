package org.xodium.vanillaplus.enchantments

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.attribute.Attribute
import org.bukkit.entity.HappyGhast
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents an object handling nimbus enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object NimbusEnchantment : EnchantmentInterface {
    private object DEFAULTS {
        const val HAPPY_GHAST_DEFAULT_FLYING_SPEED = 0.05
    }

    override fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(key.value().replaceFirstChar { it.uppercase() }.mm())
            .anvilCost(2)
            .maxLevel(1)
            .weight(2)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(25, 0))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(75, 0))
            .activeSlots(EquipmentSlotGroup.SADDLE)

    /**
     * Handles the event when an entity's equipment changes, specifically for Happy Ghasts with the nimbus enchantment.
     * @param event The event representing the change in entity equipment.
     */
    fun nimbus(event: EntityEquipmentChangedEvent) {
        val entity = event.entity as? HappyGhast ?: return
        val harness = entity.equipment.getItem(EquipmentSlot.SADDLE)

        if (harness.hasItemMeta() && harness.itemMeta.hasEnchant(get())) {
            entity.getAttribute(Attribute.FLYING_SPEED)?.baseValue = 0.1
        } else {
            entity.getAttribute(Attribute.FLYING_SPEED)?.baseValue = DEFAULTS.HAPPY_GHAST_DEFAULT_FLYING_SPEED
        }
    }
}
