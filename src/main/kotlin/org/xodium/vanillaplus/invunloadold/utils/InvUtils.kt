/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold.utils

import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.invunloadold.UnloadSummary

object InvUtils {
    fun searchItemInContainers(mat: Material, destination: Inventory, summary: UnloadSummary): Boolean {
        if (BlockUtils.Companion.doesChestContain(destination, ItemStack(mat))) {
            val amount = BlockUtils.Companion.doesChestContainCount(destination, mat)
            summary.protocolUnload(destination.location, mat, amount)
            return true
        }
        return false
    }

    private fun countInventoryContents(inv: Inventory): Int {
        var count = 0
        for (item in inv.contents) {
            if (item == null) continue
            count += item.amount
        }
        return count
    }

    fun stuffInventoryIntoAnother(
        p: Player,
        destination: Inventory,
        onlyMatchingStuff: Boolean,
        startSlot: Int,
        endSlot: Int,
        summary: UnloadSummary?
    ): Boolean {
        val source: Inventory = p.inventory
        val start = countInventoryContents(source)
        for (i in startSlot..endSlot) {
            val item = source.getItem(i)
            if (item == null) continue
            if (ShulkerUtils.isShulkerBox(item)) {
                if (destination.holder != null && destination.holder is ShulkerBox) {
                    continue
                }
            }
            source.clear(i)
            var amount = item.amount
            if (!onlyMatchingStuff || BlockUtils.Companion.doesChestContain(destination, item)) {
                for (leftover in destination.addItem(item).values) {
                    amount = amount - leftover.amount
                    source.setItem(i, leftover)
                }
                summary?.protocolUnload(destination.location, item.type, amount)
            } else {
                source.setItem(i, item)
            }
        }
        return start != countInventoryContents(source)
    }
}