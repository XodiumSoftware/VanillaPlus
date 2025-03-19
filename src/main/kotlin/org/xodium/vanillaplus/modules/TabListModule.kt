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
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
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
                Runnable { updateTabList(Audience.audience()) },
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
    fun on(event: PlayerJoinEvent) = updateTabList(event.player)

    /**
     * Update the tab list for the given audience
     * @param audience the audience to update the tab list for
     */
    private fun updateTabList(audience: Audience) {
        audience.sendPlayerListHeaderAndFooter(TODO(), TODO())
    }
}