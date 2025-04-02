/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityPortalEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.TimeUtils.ticks
import org.xodium.vanillaplus.utils.Utils.fireFmt
import org.xodium.vanillaplus.utils.Utils.mm

/**
 * Handles dimension teleportation
 */
class DimensionsModule : ModuleInterface {
    override fun enabled(): Boolean = Config.DimensionsModule.ENABLED

    /**
     * Event handler for the PlayerPortalEvent.
     *
     * @param event The PlayerPortalEvent that was triggered.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerPortalEvent) {
        val player = event.player
        if (event.cause == TeleportCause.NETHER_PORTAL) {
            when (player.world.environment) {
                World.Environment.NORMAL -> {
                    event.canCreatePortal = true
                }

                World.Environment.NETHER -> {
                    event.canCreatePortal = false
                    instance.server.scheduler.runTaskLater(instance, Runnable {
                        if (player.world != event.to.world) {
                            player.sendActionBar("Cannot create new portal link from the Nether".fireFmt().mm())
                        }
                    }, 1.ticks)
                }

                else -> return
            }
        }
    }

    /**
     * Event handler for the EntityPortalEvent.
     *
     * @param event The EntityPortalEvent that was triggered.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: EntityPortalEvent) {
        event.canCreatePortal = false
    }
}