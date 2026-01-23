package org.xodium.vanillaplus.enchantments

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling night vision enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object NightVisionEnchantment : EnchantmentInterface {
    override fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(key.displayName())
            .anvilCost(2)
            .maxLevel(1)
            .weight(2)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(25, 0))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(75, 0))
            .activeSlots(EquipmentSlotGroup.ARMOR)

    /**
     * Handles the equipment change event to apply or remove night vision effect based on the helmet enchantment.
     * @param event The EntityEquipmentChangedEvent triggered when an entity's equipment changes.
     */
    fun nightVision(event: EntityEquipmentChangedEvent) {
        val player = event.entity as? Player ?: return
        val helmet = player.inventory.helmet

        if (helmet != null && helmet.hasItemMeta() && helmet.itemMeta.hasEnchant(get())) {
            player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, true, false, true))
        } else {
            player.activePotionEffects
                .filter { potionEffect -> potionEffect.type == PotionEffectType.NIGHT_VISION }
                .forEach { potionEffect ->
                    if (potionEffect.duration == -1) {
                        player.removePotionEffect(PotionEffectType.NIGHT_VISION)
                    }
                }
        }
    }
}
