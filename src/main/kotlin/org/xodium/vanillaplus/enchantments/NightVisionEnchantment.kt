package org.xodium.vanillaplus.enchantments

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
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

    /**
     * Handles the equipment change event to apply or remove night vision effect based on the helmet enchantment.
     * @param event The EntityEquipmentChangedEvent triggered when an entity's equipment changes.
     */
    fun nightVision(event: EntityEquipmentChangedEvent) {
        val player = event.entity as Player
        val helmet = player.inventory.helmet ?: return

        if (!helmet.hasItemMeta()) return
        if (helmet.itemMeta.hasEnchant(get())) {
            player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 0, true, false, true))
        } else {
            if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                player.removePotionEffect(PotionEffectType.NIGHT_VISION)
            }
        }
    }
}
