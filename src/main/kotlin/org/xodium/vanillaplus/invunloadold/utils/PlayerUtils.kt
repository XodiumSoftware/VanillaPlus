/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold.utils

import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.invunloadold.api.InvUnloadCheckAccessEvent

object PlayerUtils {
    fun canPlayerUseChest(block: Block?, player: Player?): Boolean {
        if (instance.config.getBoolean("use-playerinteractevent")) { //TODO: use Config
            val event: PlayerInteractEvent = InvUnloadCheckAccessEvent(
                player!!,
                Action.RIGHT_CLICK_BLOCK, null, block, BlockFace.UP
            )
            instance.server.pluginManager.callEvent(event)
            if (event.useInteractedBlock() == Event.Result.DENY) return false
        }
        return true
    }
}