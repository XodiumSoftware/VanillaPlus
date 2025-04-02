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
import org.xodium.vanillaplus.utils.Utils.mm

/**
 * Customizes the server's Message of the Day (MOTD) that appears in the server list.
 * When enabled, it replaces the default MOTD with a configured message from the plugin's configuration.
 */
class MotdModule : ModuleInterface {
    override fun enabled(): Boolean = Config.MotdModule.ENABLED

    /**
     * Event handler for the ServerListPingEvent.
     * When the event is triggered, it replaces the default MOTD with a custom message.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: ServerListPingEvent) = event.motd(Config.MotdModule.MOTD.joinToString("\n").mm())
}