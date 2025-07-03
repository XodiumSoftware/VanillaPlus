/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.ServerListPingEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt

/** Represents a module handling MOTD mechanics within the system. */
class MotdModule : ModuleInterface<MotdModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: ServerListPingEvent) {
        if (!enabled()) return
        event.motd(config.motd.joinToString("\n").mm())
    }

    data class Config(
        override var enabled: Boolean = true,
        val motd: List<String> = listOf(
            "<b>Ultimate Private SMP</b>".fireFmt(),
            "<b>➤ WELCOME BACK LADS!</b>".mangoFmt(),
        )
    ) : ModuleInterface.Config
}