/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold.utils

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*

object PlayerUtils {
    private val deniedAccess: MutableSet<Pair<UUID, Block>> = Collections.synchronizedSet(mutableSetOf())

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK && event.clickedBlock != null) {
            if (event.clickedBlock!!.type.name.contains("CHEST")
            ) {
                deny(event.player, event.clickedBlock!!)
            } else {
                clear(event.player, event.clickedBlock!!)
            }
        }
    }

    fun canPlayerUseChest(block: Block?, player: Player?): Boolean {
        if (block == null || player == null) return false
        return !isDenied(player, block)
    }

    private fun deny(player: Player, block: Block) {
        deniedAccess.add(player.uniqueId to block)
    }

    private fun isDenied(player: Player, block: Block): Boolean {
        return deniedAccess.contains(player.uniqueId to block)
    }

    private fun clear(player: Player, block: Block) {
        deniedAccess.remove(player.uniqueId to block)
    }
}
