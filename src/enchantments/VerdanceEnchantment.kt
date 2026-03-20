package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.block.data.Ageable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling verdance enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object VerdanceEnchantment : EnchantmentInterface {
    override fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(key.displayName())
            .anvilCost(2)
            .maxLevel(1)
            .weight(2)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(25, 0))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(75, 0))
            .activeSlots(EquipmentSlotGroup.MAINHAND)

    /**
     * Automatically replants a crop block after it has been fully grown and harvested.
     * @param event The BlockBreakEvent triggered when a block is broken.
     */
    fun verdance(event: BlockBreakEvent) {
        val block = event.block
        val ageable = block.blockData as? Ageable ?: return
        val itemInHand = event.player.inventory.itemInMainHand

        if (ageable.age < ageable.maximumAge) return
        if (!itemInHand.hasItemMeta() || !itemInHand.itemMeta.hasEnchant(get())) return

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable { block.blockData = ageable.apply { age = 0 } },
            2,
        )
    }

    /**
     * Converts all items in the player's inventory, armor, offhand, and ender chest
     * from the legacy Replant enchantment key to Verdance.
     * @param player The player whose items should be migrated.
     */
    fun migrate(player: Player) {
        val old = ReplantEnchantment.get()
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
