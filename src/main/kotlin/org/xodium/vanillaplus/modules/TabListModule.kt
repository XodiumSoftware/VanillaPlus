/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.audience.Audience
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.interfaces.ModuleInterface

class TabListModule : ModuleInterface {
    override fun enabled(): Boolean = Config.TabListModule.ENABLED

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerJoinEvent) = Audience.audience(event.player)
        .sendPlayerListHeaderAndFooter(Config.TabListModule.HEADER.mm(), Config.TabListModule.FOOTER.mm())
}