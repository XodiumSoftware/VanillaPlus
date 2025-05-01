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
import org.xodium.vanillaplus.VanillaPlus
import org.xodium.vanillaplus.invunloadold.Main
import org.xodium.vanillaplus.invunloadold.api.InvUnloadCheckAccessEvent

object PlayerUtils {
    fun canPlayerUseChest(block: Block?, player: Player?, main: Main): Boolean {
        if (main.config.getBoolean("use-playerinteractevent")) {
            val event: PlayerInteractEvent = InvUnloadCheckAccessEvent(
                player!!,
                Action.RIGHT_CLICK_BLOCK, null, block, BlockFace.UP
            )
            VanillaPlus.Companion.instance.server.pluginManager.callEvent(event)
            if (event.useInteractedBlock() == Event.Result.DENY) {
                return false
            }
        }
        return true
    }
}