/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.interfaces.ModuleInterface

/**
 * Module to handle dimension teleportation
 */
class DimensionsModule : ModuleInterface {
    /**
     * Returns true if the module is enabled in the plugin's configuration.
     */
    override fun enabled(): Boolean = Config.DimensionsModule.ENABLED

    /**
     * Event handler for the PlayerPortalEvent.
     * When the event is triggered, it cancels the event and teleports the player to the corresponding dimension.
     *
     * @param event The PlayerPortalEvent that was triggered.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerPortalEvent) {
        if (event.cause == TeleportCause.NETHER_PORTAL) {
            event.canCreatePortal = false
        }
    }

    /**
     * Event handler for the EntityPortalEvent.
     * When the event is triggered, it cancels the event to prevent any unwanted teleportation.
     *
     * @param event The EntityPortalEvent that was triggered.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: EntityPortalEvent) {
        event.canCreatePortal = false
    }
}