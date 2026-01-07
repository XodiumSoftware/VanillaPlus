package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.Utils.displayName

/** Represents an object handling pickup enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object PickupEnchantment : EnchantmentInterface {
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
     * Handles the block break event to automatically pick up drops when the tool has the Pickup enchantment.
     * @param event The BlockBreakEvent triggered when a block is broken.
     */
    fun pickup(event: BlockDropItemEvent) {
        val player = event.player
        val itemInHand = player.inventory.itemInMainHand

        if (!itemInHand.hasItemMeta() || !itemInHand.itemMeta.hasEnchant(get())) return

        transferItemEntitiesToInventory(player, event.items)
    }

    /**
     * Transfers item entities to a player's inventory using the same removeIf pattern.
     * @param player The player whose inventory to transfer to
     * @param items The mutable collection of item entities to transfer
     */
    fun transferItemEntitiesToInventory(
        player: Player,
        items: MutableCollection<Item>,
    ) {
        items.removeIf { item ->
            val remaining = player.inventory.addItem(item.itemStack)
            val remainingItem = remaining[0] ?: return@removeIf true

            remainingItem.takeIf { it.amount > 0 }?.let { item.itemStack = it } == null
        }
    }
}
