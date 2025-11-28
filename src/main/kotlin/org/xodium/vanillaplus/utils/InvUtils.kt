package org.xodium.vanillaplus.utils

import org.bukkit.Tag
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/** Inventory utilities. */
internal object InvUtils {
    /**
     * Transfer items from source to destination inventory.
     * @param source The source inventory.
     * @param destination The destination inventory.
     * @param startSlot The starting slot in source inventory.
     * @param endSlot The ending slot in source inventory.
     * @param onlyMatching If true, only transfer items that already exist in the destination.
     * @param enchantmentChecker Function to check if enchantments match.
     * @return Pair<Boolean success, Int itemsTransferred>
     */
    fun transferItems(
        source: Inventory,
        destination: Inventory,
        startSlot: Int = 9,
        endSlot: Int = 35,
        onlyMatching: Boolean = false,
        enchantmentChecker: (ItemStack, ItemStack) -> Boolean = { _, _ -> true },
    ): Boolean {
        var moved = false

        for (i in startSlot..endSlot) {
            val item = source.getItem(i) ?: continue

            if (!isValidTransfer(item, destination)) continue
            if (onlyMatching && !containsMatchingItem(destination, item, enchantmentChecker)) continue

            val leftovers = destination.addItem(item)
            val movedAmount = item.amount - leftovers.values.sumOf { it.amount }

            if (movedAmount > 0) {
                moved = true
                source.clear(i)
                leftovers.values.firstOrNull()?.let { source.setItem(i, it) }
            }
        }

        return moved
    }

    /**
     * Check if transferring an item would be valid (not putting shulker in shulker, etc.)
     * @param item The item to transfer.
     * @param destination The destination inventory.
     * @return True if the transfer is valid, false otherwise.
     */
    private fun isValidTransfer(
        item: ItemStack,
        destination: Inventory,
    ): Boolean = !(Tag.SHULKER_BOXES.isTagged(item.type) && destination.holder is ShulkerBox)

    /**
     * Check if inventory contains an item with matching type and enchantments.
     * @param inventory The inventory to check.
     * @param item The item to match.
     * @param enchantmentChecker Function to check enchantment compatibility.
     * @return True if a matching item is found.
     */
    private fun containsMatchingItem(
        inventory: Inventory,
        item: ItemStack,
        enchantmentChecker: (ItemStack, ItemStack) -> Boolean,
    ): Boolean =
        inventory.contents
            .asSequence()
            .filterNotNull()
            .any { it.type == item.type && enchantmentChecker(item, it) }
}
