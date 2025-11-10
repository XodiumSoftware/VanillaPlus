package org.xodium.vanillaplus.utils

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Container
import org.bukkit.block.ShulkerBox
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/** Inventory utilities. */
internal object InvUtils {
    /**
     * Counts the total number of items in the given inventory.
     * @param inventory The inventory to count items in.
     * @return The total number of items in the inventory.
     */
    fun countContents(inventory: Inventory): Int = inventory.contents.filterNotNull().sumOf { it.amount }

    /**
     * Get the amount of a specific material in an inventory.
     * @param inventory The inventory to check.
     * @param material The material to count.
     * @return The amount of the material in the inventory.
     */
    fun getMaterialCount(
        inventory: Inventory,
        material: Material,
    ): Int =
        inventory.contents
            .filter { it?.type == material }
            .sumOf { it?.amount ?: 0 }

    /**
     * Check if an inventory contains an item with the same type.
     * @param inventory The inventory to check.
     * @param item The item to check for.
     * @return True if the inventory contains the item type, false otherwise.
     */
    fun containsItemType(
        inventory: Inventory,
        item: ItemStack,
    ): Boolean = inventory.contents.any { it?.type == item.type }

    /**
     * Check if transferring an item would be valid (not putting shulker in shulker, etc.)
     * @param item The item to transfer.
     * @param destination The destination inventory.
     * @return True if the transfer is valid, false otherwise.
     */
    fun isValidTransfer(
        item: ItemStack,
        destination: Inventory,
    ): Boolean = !(Tag.SHULKER_BOXES.isTagged(item.type) && destination.holder is ShulkerBox)

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
    ): Pair<Boolean, Int> {
        var moved = false
        var totalTransferred = 0

        for (i in startSlot..endSlot) {
            val item = source.getItem(i) ?: continue

            if (!isValidTransfer(item, destination)) continue
            if (onlyMatching && !containsMatchingItem(destination, item, enchantmentChecker)) continue

            val leftovers = destination.addItem(item)
            val movedAmount = item.amount - leftovers.values.sumOf { it.amount }

            if (movedAmount > 0) {
                moved = true
                totalTransferred += movedAmount
                source.clear(i)
                leftovers.values.firstOrNull()?.let { source.setItem(i, it) }
            }
        }

        return Pair(moved, totalTransferred)
    }

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

    /**
     * Search containers for specific material.
     * @param containers List of containers to search.
     * @param material The material to search for.
     * @param enchantmentChecker Function to check enchantment compatibility.
     * @return List of containers that contain the material.
     */
    fun searchContainersForMaterial(
        containers: List<Container>,
        material: Material,
        enchantmentChecker: (ItemStack, ItemStack) -> Boolean,
    ): List<Container> =
        containers.filter { container ->
            container.inventory.contents.any { item ->
                item?.type == material && enchantmentChecker(ItemStack(material), item)
            }
        }
}
