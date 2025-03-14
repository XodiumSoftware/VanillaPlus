/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface

class DimensionsModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.DimensionsModule().enabled

    @EventHandler
    fun on(event: PlayerPortalEvent) {
        val player = event.player
        if (event.cause == TeleportCause.NETHER_PORTAL) {
            event.isCancelled = true
            player.server.dispatchCommand(player, "cmi rt world_nether")
        } else if (event.cause == TeleportCause.END_PORTAL) {
            event.isCancelled = true
            player.server.dispatchCommand(player, "cmi rt world_the_end")
        }
    }
}