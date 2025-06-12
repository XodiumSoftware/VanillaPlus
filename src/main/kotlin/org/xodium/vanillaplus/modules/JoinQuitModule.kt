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
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.SkinUtils.faceToMM

/** Represents a module handling join/quit mechanics within the system. */
class JoinQuitModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.joinQuitModule.enabled

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return

        val player = event.player
        instance.server.onlinePlayers
            .filter { it.uniqueId != player.uniqueId }
            //TODO: message in Conflict with CMI, disable in CMI or move away.
            .forEach { it.sendMessage("<gold>[<green>+<gold>]<reset> ${player.displayName()}".mm()) }

        val faceLines = player.faceToMM().lines()
        var imageIndex = 1
        val welcomeText =
            Regex("<image>").replace(ConfigManager.data.joinQuitModule.welcomeText) { "<image${imageIndex++}>" }
        val imageResolvers = faceLines.mapIndexed { i, line -> Placeholder.component("image${i + 1}", line.mm()) }
        player.sendMessage(
            welcomeText.mm(
                Placeholder.component("player", player.displayName()),
                *imageResolvers.toTypedArray()
            )
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