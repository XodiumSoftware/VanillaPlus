/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package de.jeff_media.InvUnload

import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.invunloadold.BlackList
import org.xodium.vanillaplus.invunloadold.BlockUtils
import org.xodium.vanillaplus.invunloadold.Main
import org.xodium.vanillaplus.invunloadold.utils.ShulkerUtils.isShulkerBox

object InvUtils {
    fun searchItemInContainers(mat: Material, destination: Inventory, summary: UnloadSummary): Boolean {
        if (BlockUtils.doesChestContain(destination, ItemStack(mat))) {
            val amount = BlockUtils.doesChestContainCount(destination, mat)

            summary.protocolUnload(destination.location, mat, amount)
            return true
        }
        return false
    }

    fun inventoryToArrayList(source: Inventory): ArrayList<ItemStack?> {
        val sourceItems = ArrayList<ItemStack?>()
        for (item in source.contents) {
            if (item == null) continue
            sourceItems.add(item)
        }
        source.clear()
        return sourceItems
    }

    fun countInventoryContents(inv: Inventory): Int {
        var count = 0
        for (item in inv.contents) {
            if (item == null) continue
            count += item.amount
        }
        return count
    }

    /**
     * Part of API. Puts everything from the player inventory inside the destination inventory.
     *
     * @param p                 Player from whom to take the items
     * @param destination       Destination inventory
     * @param onlyMatchingStuff When true, only move items that already are inside the destination inventory
     * @param startSlot         Do not modify player inventory before this slot
     * @param endSlot           Do not modify player inventory after this slot
     * @param summary           UnloadSummary object. Can be null
     * @return
     */
    fun stuffInventoryIntoAnother(
        main: Main,
        p: Player,
        destination: Inventory,
        onlyMatchingStuff: Boolean,
        startSlot: Int,
        endSlot: Int,
        summary: UnloadSummary?
    ): Boolean {
        val source: Inventory = p.inventory
        val blackList: BlackList = main.getPlayerSetting(p).getBlacklist()

        val start = countInventoryContents(source)
        for (i in startSlot..endSlot) {
            val item = source.getItem(i)
            if (item == null) continue
            if (MinepacksHook.isMinepacksBackpack(item)) continue
            if (main.inventoryPagesHook.isButton(item)) continue
            if (blackList.contains(item.type)) continue

            if (isShulkerBox(item)) {
                if (destination.holder != null && destination.holder is ShulkerBox) {
                    continue
                }
            }

            source.clear(i)
            var amount = item.amount
            if (!onlyMatchingStuff || BlockUtils.doesChestContain(destination, item)) {
                main.coreProtectHook.logCoreProtect(p.name, destination)
                for (leftover in destination.addItem(item).values) {
                    amount = amount - leftover.amount
                    source.setItem(i, leftover)
                }
                if (summary != null) summary.protocolUnload(destination.location, item.type, amount)
            } else {
                source.setItem(i, item)
            }
        }
        return start != countInventoryContents(source)
    }
}
