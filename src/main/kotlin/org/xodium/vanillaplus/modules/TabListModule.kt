/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a module handling tab-list mechanics within the system. */
class TabListModule : ModuleInterface {
    override fun enabled(): Boolean = Config.TabListModule.ENABLED

    //TODO: weather not updating correctly, TPS might have the same issue.

    init {
        if (enabled()) {
            instance.server.scheduler.runTaskTimer(
                instance,
                Runnable {
                    instance.server.onlinePlayers.forEach {
                        updateTabList(it)
                        updatePlayerDisplayName(it)
                    }
                },
                Config.TabListModule.INIT_DELAY,
                Config.TabListModule.INTERVAL
            )
        }
    }

    /**
     * Update the tab list for all players.
     * @param event the PlayerJoinEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        updateTabList(event.player)
        updatePlayerDisplayName(event.player)
    }

    /**
     * Update the player's display name in the tab list.
     * @param player the player to update.
     */
    private fun updatePlayerDisplayName(player: Player) = player.playerListName(player.displayName())

    /**
     * Update the tab list for the given audience.
     * @param audience the audience to update the tab list for.
     */
    private fun updateTabList(audience: Audience) {
        val joinConfig = JoinConfiguration.separator(Component.newline())
        audience.sendPlayerListHeaderAndFooter(
            Component.join(joinConfig, Config.TabListModule.HEADER.mm()),
            Component.join(joinConfig, Config.TabListModule.FOOTER.mm())
        )
    }
}