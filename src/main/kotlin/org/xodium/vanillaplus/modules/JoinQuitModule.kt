/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a module handling join/quit mechanics within the system. */
class JoinQuitModule : ModuleInterface {
    override fun enabled(): Boolean = Config.JoinQuitModule.ENABLED

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return

        val player = event.player
        instance.server.onlinePlayers
            .filter { it.uniqueId != player.uniqueId }
            .forEach { it.sendMessage("<gold>[<green>+<gold>]<reset> ${player.displayName()}".mm()) }

        player.sendMessage(
            Config.JoinQuitModule.WELCOME_TEXT.mm(Placeholder.component("player", player.displayName()))
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerQuitEvent) {
        if (!enabled()) return

        val player = event.player
        instance.server.onlinePlayers
            .filter { it.uniqueId != player.uniqueId }
            .forEach { it.sendMessage("<gold>[<red>-<gold>]<reset> ${player.displayName()}".mm()) }
    }
}