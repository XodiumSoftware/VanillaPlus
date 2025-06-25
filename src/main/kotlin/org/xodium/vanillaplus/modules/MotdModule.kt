/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.ServerListPingEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a module handling MOTD mechanics within the system. */
class MotdModule : ModuleInterface<MotdModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: ServerListPingEvent) {
        if (!enabled()) return
        event.motd(ConfigManager.data.motdModule.motd.joinToString("\n").mm())
    }

    data class Config(
        override val enabled: Boolean = true
    ) : ModuleInterface.Config
}