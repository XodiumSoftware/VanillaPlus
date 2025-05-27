/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.hooks

import de.jeff_media.chestsort.api.ChestSortAPI
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.xodium.vanillaplus.Config

/** ChestSortHook. */
object ChestSortHook {
    /**
     * Check if the player should sort inventory.
     * @param player Player.
     * @return Boolean.
     */
    fun shouldSort(player: Player): Boolean =
        Config.InvUnloadModule.USE_CHESTSORT && ChestSortAPI.hasSortingEnabled(player)

    /**
     * Sort block inventory.
     * @param block Block.
     */
    fun sort(block: Block) {
        if (!Config.InvUnloadModule.USE_CHESTSORT) return
        if (block.state !is Container) return
        ChestSortAPI.sortInventory((block.state as Container).inventory)
    }

    /**
     * Sort inventory.
     * @param inventory Inventory.
     */
    fun sort(inventory: Inventory) {
        if (!Config.InvUnloadModule.USE_CHESTSORT) return
        ChestSortAPI.sortInventory(inventory)
    }
}