/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold.utils

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.invunloadold.UnloadSummary

object InvUtils {
    fun searchItemInContainers(mat: Material, destination: Inventory, summary: UnloadSummary): Boolean {
        if (BlockUtils.doesChestContain(destination, ItemStack(mat))) {
            val amount = BlockUtils.doesChestContainCount(destination, mat)
            destination.location?.let { summary.protocolUnload(it, mat, amount) }
            return true
        }
        return false
    }

    private fun countInventoryContents(inv: Inventory): Int = inv.contents.filterNotNull().sumOf { it.amount }

    fun stuffInventoryIntoAnother(
        p: Player,
        destination: Inventory,
        onlyMatchingStuff: Boolean,
        startSlot: Int,
        endSlot: Int,
        summary: UnloadSummary?
    ): Boolean {
        val source = p.inventory
        val start = countInventoryContents(source)
        for (i in startSlot..endSlot) {
            val item = source.getItem(i) ?: continue
            if (Tag.SHULKER_BOXES.isTagged(item.type) && destination.holder is ShulkerBox) continue
            if (!onlyMatchingStuff || BlockUtils.doesChestContain(destination, item)) {
                source.clear(i)
                var amount = item.amount
                for (leftover in destination.addItem(item).values) {
                    amount -= leftover.amount
                    source.setItem(i, leftover)
                }
                destination.location?.let { summary?.protocolUnload(it, item.type, amount) }
            }
        }
        return start != countInventoryContents(source)
    }
}