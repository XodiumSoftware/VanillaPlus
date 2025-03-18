/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface

/**
 * Module to handle dimension teleportation
 */
class DimensionsModule : ModuleInterface {
    /**
     * Returns true if the module is enabled in the plugin's configuration.
     */
    override fun enabled(): Boolean = ConfigData.DimensionsModule().enabled

    /**
     * Event handler for the PlayerPortalEvent.
     * When the event is triggered, it cancels the event and teleports the player to the corresponding dimension.
     */
    @EventHandler
    fun on(event: PlayerPortalEvent) {
        val player = event.player
        val server = player.server
        val environment = player.world.environment
        val cause = event.cause
        val destination = when {
            cause == TeleportCause.NETHER_PORTAL && environment == World.Environment.NORMAL -> "world_nether"
            cause == TeleportCause.NETHER_PORTAL && environment == World.Environment.NETHER -> "world"
            cause == TeleportCause.END_PORTAL && environment == World.Environment.NORMAL -> "world_the_end"
            cause == TeleportCause.END_PORTAL && environment == World.Environment.THE_END -> "world"
            else -> null
        }
        if (destination != null) {
            event.isCancelled = true
            server.dispatchCommand(player, "cmi rt $destination")
        }
    }
}