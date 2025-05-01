/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package de.jeff_media.InvUnload

import de.jeff_media.InvUnload.API.InvUnloadCheckAccessEvent
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.Action
import org.xodium.vanillaplus.invunloadold.Main

object PlayerUtils {
    // Calls PlayerInteractEvent to see if access is blocked by 3rd party plugins
    fun canPlayerUseChest(block: Block?, player: Player?, main: Main): Boolean {
        if (main.plotSquaredHook.isBlockedByPlotSquared(block, player)) {
            return false
        }

        if (main.getConfig().getBoolean("use-playerinteractevent")) {
            val event: PlayerInteractEvent = InvUnloadCheckAccessEvent(
                player,
                Action.RIGHT_CLICK_BLOCK, null, block, BlockFace.UP
            )

            Bukkit.getPluginManager().callEvent(event)

            SpartanHook.cancelSpartanEventCancel(event)

            if (event.useInteractedBlock() == Event.Result.DENY) {
                return false
            }
        }

        return true
    }
}
