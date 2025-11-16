package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import org.bukkit.Location
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents an object handling pickup enchantment implementation within the system. */
@Suppress("UnstableApiUsage")
internal object PickupEnchantment : EnchantmentInterface {
    val validPickupBreaks = mutableSetOf<Location>()

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
    fun pickup(event: BlockDropItemEvent) {
        val player = event.player
        val itemInHand = player.inventory.itemInMainHand
        val block = event.block

        if (!itemInHand.hasItemMeta() ||
            !itemInHand.itemMeta.hasEnchant(get()) ||
            !validPickupBreaks.remove(block.location)
        ) {
            return
        }

        val inventory = player.inventory

        event.items.removeIf { item ->
            val remaining = inventory.addItem(item.itemStack)
            val remainingItem = remaining[0] ?: return@removeIf true

            remainingItem.takeIf { it.amount > 0 }?.let { item.itemStack = it } == null
        }
    }

    /**
     * Checks if the block being broken is done with the preferred tool for that block type.
     * @param event The BlockBreakEvent triggered when a block is broken.
     */
    fun checkPreferredTool(event: BlockBreakEvent) {
        val player = event.player
        val itemInHand = player.inventory.itemInMainHand
        val block = event.block

        if (itemInHand.hasItemMeta() &&
            itemInHand.itemMeta.hasEnchant(get()) &&
            // NOTE: method is broken.
            // NOTE: returns true on block=oak_log and itemInHand=iron_pickaxe while mc docs say it should be false.
            // NOTE: also when fixed, check if we can call isPreferredTool in the BlockDropItemEvent instead of here.
            block.isPreferredTool(itemInHand)
        ) {
            validPickupBreaks.add(block.location)
        }
    }
}
