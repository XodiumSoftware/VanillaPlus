/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.hooks

import de.jeff_media.chestsort.api.ChestSortAPI
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.xodium.vanillaplus.Config


/**
 * ChestSortHook
 */
class ChestSortHook {
    /**
     * Check if player should sort inventory
     *
     * @param player Player
     * @return Boolean
     */
    fun shouldSort(player: Player): Boolean =
        Config.InvUnloadModule.USE_CHESTSORT && ChestSortAPI.hasSortingEnabled(player)

    /**
     * Sort inventory
     *
     * @param block Block
     */
    fun sort(block: Block) {
        if (!Config.InvUnloadModule.USE_CHESTSORT) return
        TODO("add Registry")
        if (!BlockUtils.isChestLikeBlock(block.type)) return
        ChestSortAPI.sortInventory((block.state as Container).inventory)
    }
}