/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils.invunload

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.modules.InvUnloadModule

//TODO: Move to a more generic location.
object InvUtils {
    /**
     * Searches for a specific item in the given inventory and its containers.
     * @param material The material to search for.
     * @param destination The inventory to search in.
     * @param summary The InvUnloadModule instance for protocol unloading.
     * @return True if the item was found in the inventory or its containers, false otherwise.
     */
    fun searchItemInContainers(material: Material, destination: Inventory, summary: InvUnloadModule): Boolean {
        if (BlockUtils.doesChestContain(destination, ItemStack(material))) {
            val amount = BlockUtils.doesChestContainCount(destination, material)
            destination.location?.let { summary.protocolUnload(it, material, amount) }
            return true
        }
        return false
    }

    /**
     * Counts the total number of items in the given inventory.
     * @param inv The inventory to count items in.
     * @return The total number of items in the inventory.
     */
    private fun countInventoryContents(inv: Inventory): Int = inv.contents.filterNotNull().sumOf { it.amount }

    /**
     * Moves items from the player's inventory to another inventory.
     * @param player The player whose inventory is being moved.
     * @param destination The destination inventory to move items into.
     * @param onlyMatchingStuff If true, only moves items that match the destination's contents.
     * @param startSlot The starting slot in the player's inventory to move items from.
     * @param endSlot The ending slot in the player's inventory to move items from.
     * @param summary The InvUnloadModule instance for protocol unloading.
     * @return True if items were moved, false otherwise.
     */
    fun stuffInventoryIntoAnother(
        player: Player,
        destination: Inventory,
        onlyMatchingStuff: Boolean,
        startSlot: Int,
        endSlot: Int,
        summary: InvUnloadModule?
    ): Boolean {
        val source = player.inventory
        val initialCount = countInventoryContents(source)
        var moved = false

        for (i in startSlot..endSlot) {
            val item = source.getItem(i) ?: continue
            if (Tag.SHULKER_BOXES.isTagged(item.type) && destination.holder is ShulkerBox) continue
            if (onlyMatchingStuff && !BlockUtils.doesChestContain(destination, item)) continue

            val leftovers = destination.addItem(item)
            val movedAmount = item.amount - leftovers.values.sumOf { it.amount }
            if (movedAmount > 0) {
                moved = true
                source.clear(i)
                leftovers.values.firstOrNull()?.let { source.setItem(i, it) }
                destination.location?.let { summary?.protocolUnload(it, item.type, movedAmount) }
            }
        }
        return moved && initialCount != countInventoryContents(source)
    }
}