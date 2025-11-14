package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents an object handling pickup enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object PickupEnchantment : EnchantmentInterface {
    override fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(key.value().replaceFirstChar { it.uppercase() }.mm())
            .anvilCost(2)
            .maxLevel(1)
            .weight(2)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(25, 0))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(75, 0))
            .activeSlots(EquipmentSlotGroup.MAINHAND)

    /**
     * Handles the block break event to automatically pick up drops when the tool has the Pickup enchantment.
     * @param event The BlockBreakEvent triggered when a block is broken.
     */
    fun pickup(event: BlockBreakEvent) {
        val player = event.player
        val itemInHand = player.inventory.itemInMainHand

        if (!itemInHand.hasItemMeta() || !itemInHand.itemMeta.hasEnchant(get())) return

        event.isDropItems = false

        for (drop in event.block.drops) {
            val remaining = player.inventory.addItem(drop)

            for (item in remaining.values) player.world.dropItemNaturally(player.location, item)
        }
    }
}
