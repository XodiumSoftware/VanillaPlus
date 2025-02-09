/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.ServerListPingEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.interfaces.ModuleInterface

class MotdModule : ModuleInterface {
    override fun enabled(): Boolean = Config.MotdModule.ENABLED

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: ServerListPingEvent) = event.motd(
        Config.MotdModule.MOTD.joinToString(separator = "\n")
            .replace("\\n", "\n")
            .mm()
    )
}