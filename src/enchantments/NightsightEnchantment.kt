package org.xodium.vanillaplus.enchantments

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling nightsight enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object NightsightEnchantment : EnchantmentInterface {
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
    fun nightsight(event: EntityEquipmentChangedEvent) {
        val player = event.entity as? Player ?: return
        val helmet = player.inventory.helmet

        if (helmet != null && helmet.hasItemMeta() && helmet.itemMeta.hasEnchant(get())) {
            player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, -1, 0, true, false, true))
        } else {
            player.activePotionEffects
                .filter { it.type == PotionEffectType.NIGHT_VISION }
                .forEach {
                    if (it.duration == -1) player.removePotionEffect(PotionEffectType.NIGHT_VISION)
                }
        }
    }

    /**
     * Converts all items in the player's inventory, armor, offhand, and ender chest
     * from the legacy NightVision enchantment key to Nightsight.
     * @param player The player whose items should be migrated.
     */
    fun migrate(player: Player) {
        val old = NightVisionEnchantment.get()
        val new = get()
        val inv = player.inventory

        migrateSlots(inv, old, new)

        val armor = inv.armorContents.clone()
        armor.forEach { it?.let { item -> migrateItem(item, old, new) } }
        inv.armorContents = armor

        val offhand = inv.itemInOffHand
        if (migrateItem(offhand, old, new)) inv.setItemInOffHand(offhand)

        migrateSlots(player.enderChest, old, new)
    }

    private fun migrateSlots(
        inventory: Inventory,
        old: Enchantment,
        new: Enchantment,
    ) {
        for (i in 0 until inventory.size) {
            val item = inventory.getItem(i) ?: continue
            if (migrateItem(item, old, new)) inventory.setItem(i, item)
        }
    }

    private fun migrateItem(
        item: ItemStack,
        old: Enchantment,
        new: Enchantment,
    ): Boolean {
        val meta = item.itemMeta ?: return false
        return when {
            meta is EnchantmentStorageMeta && meta.hasStoredEnchant(old) -> {
                val level = meta.getStoredEnchantLevel(old)
                meta.removeStoredEnchant(old)
                meta.addStoredEnchant(new, level, true)
                item.itemMeta = meta
                true
            }

            meta.hasEnchant(old) -> {
                val level = meta.getEnchantLevel(old)
                meta.removeEnchant(old)
                meta.addEnchant(new, level, true)
                item.itemMeta = meta
                true
            }

            else -> {
                false
            }
        }
    }
}
