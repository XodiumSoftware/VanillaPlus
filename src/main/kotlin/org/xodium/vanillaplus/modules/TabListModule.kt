/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.hooks.CMIHook
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.TimeUtils.seconds
import org.xodium.vanillaplus.utils.TimeUtils.ticks

/**
 * Tab list module
 */
class TabListModule : ModuleInterface {
    /**
     * @return true if the module is enabled
     */
    override fun enabled(): Boolean = Config.TabListModule.ENABLED

    init {
        if (enabled()) {
            instance.server.scheduler.runTaskTimer(
                instance,
                Runnable {
                    instance.server.onlinePlayers.forEach { player ->
                        updateTabList(player)
                        updatePlayerDisplayName(player)
                    }
                },
                0.ticks,
                10.seconds
            )
        }
    }

    /**
     * Update the tab list for all players
     * @param event the player join event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        updateTabList(event.player)
        updatePlayerDisplayName(event.player)
    }

    /**
     * Update the player's display name in the tab list
     * @param player the player to update
     */
    private fun updatePlayerDisplayName(player: Player) {
        val displayName = CMIHook.CMI_USER_DISPLAY_NAME
        val legacyDisplayName = PlaceholderAPI.setPlaceholders(player, displayName)
        if (legacyDisplayName != displayName) {
            player.playerListName(Utils.legacyToComponent(legacyDisplayName))
        }
    }

    /**
     * Update the tab list for the given audience
     * @param audience the audience to update the tab list for
     */
    private fun updateTabList(audience: Audience) {
        val joinConfig = JoinConfiguration.separator(Component.newline())
        audience.sendPlayerListHeaderAndFooter(
            Component.join(joinConfig, Config.TabListModule.HEADER.mm()),
            Component.join(joinConfig, Config.TabListModule.FOOTER.mm())
        )
    }
}