/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.hooks

import de.jeff_media.chestsort.api.ChestSortAPI
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.Player

/** ChestSortHook. */
object ChestSortHook {
    /**
     * Check if the player should sort inventory.
     * @param player Player.
     * @return Boolean.
     */
    fun shouldSort(player: Player): Boolean = ChestSortAPI.hasSortingEnabled(player)

    /**
     * Sort block inventory.
     * @param player Player.
     * @param block Block.
     */
    fun sort(player: Player, block: Block) {
        if (!shouldSort(player)) return
        if (block.state !is Container) return
        ChestSortAPI.sortInventory((block.state as Container).inventory)
    }
}