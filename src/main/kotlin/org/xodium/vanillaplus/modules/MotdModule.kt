/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.ServerListPingEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.FmtUtils.mm

/**
 * Handles functionality related to the Message of the Day (MOTD) module.
 * This module allows for the customization of the server's MOTD displayed during connection attempts.
 */
class MotdModule : ModuleInterface {
    override fun enabled(): Boolean = Config.MotdModule.ENABLED

    /**
     * Event handler for the ServerListPingEvent.
     * When the event is triggered, it replaces the default MOTD with a custom message.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: ServerListPingEvent): Unit = event.motd(Config.MotdModule.MOTD.joinToString("\n").mm())
}