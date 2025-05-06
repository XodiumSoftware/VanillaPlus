/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils.invunload

import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object PlayerUtils {
    private data class PlayerBlockKey(val playerId: UUID, val blockLocation: Location)

    private val deniedAccess = Collections.newSetFromMap(ConcurrentHashMap<PlayerBlockKey, Boolean>())

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val block = event.clickedBlock
        if (event.action == Action.RIGHT_CLICK_BLOCK && block != null) {
            val key = PlayerBlockKey(event.player.uniqueId, block.location)
            if (block.type.name.contains("CHEST")) deniedAccess.add(key) else deniedAccess.remove(key)
        }
    }

    fun canPlayerUseChest(block: Block?, player: Player?): Boolean {
        if (block == null || player == null) return false
        return !deniedAccess.contains(PlayerBlockKey(player.uniqueId, block.location))
    }
}
