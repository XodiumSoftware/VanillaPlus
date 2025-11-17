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
    const val DEFAULT_FLY_SPEED = 0.05

    override fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(key.value().replaceFirstChar { it.uppercase() }.mm())
            .anvilCost(2)
            .maxLevel(5)
            .weight(2)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(25, 5))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(75, 10))
            .activeSlots(EquipmentSlotGroup.SADDLE)

    /**
     * Handles the event when an entity's equipment changes, specifically for Happy Ghasts with the nimbus enchantment.
     * @param event The event representing the change in entity equipment.
     */
    fun nimbus(event: EntityEquipmentChangedEvent) {
        val entity = event.entity as? HappyGhast ?: return
        val harness = entity.equipment.getItem(EquipmentSlot.BODY)
        val attribute = entity.getAttribute(Attribute.FLYING_SPEED)

        if (harness.hasItemMeta() && harness.itemMeta.hasEnchant(get())) {
            val level = harness.itemMeta.getEnchantLevel(get())
            val speedMultiplier = getSpeedMultiplier(level)
            attribute?.baseValue = DEFAULT_FLY_SPEED * speedMultiplier
        } else {
            attribute?.baseValue = DEFAULT_FLY_SPEED
        }
    }

    /**
     * Calculates the flying speed multiplier based on the enchantment level.
     * @param level The enchantment level (1-5)
     * @return The flying speed multiplier for the given level
     */
    private fun getSpeedMultiplier(level: Int): Double =
        when (level) {
            1 -> 1.0
            2 -> 1.5
            3 -> 2.0
            4 -> 2.5
            5 -> 3.0
            else -> 1.0
        }
}
