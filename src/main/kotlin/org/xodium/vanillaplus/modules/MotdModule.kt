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
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a module handling MOTD mechanics within the system. */
class MotdModule : ModuleInterface {
    override fun enabled(): Boolean = Config.MotdModule.ENABLED

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: ServerListPingEvent) {
        if (!enabled()) return
        event.motd(Config.MotdModule.MOTD.joinToString("\n").mm())
    }
}