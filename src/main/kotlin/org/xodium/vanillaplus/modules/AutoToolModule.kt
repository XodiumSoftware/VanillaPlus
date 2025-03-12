/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface

class AutoToolModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.AutoToolModule().enabled

    @EventHandler
    fun on(event: PlayerQuitEvent) {
        val player = event.player
//        TODO: use correct database settings.
        main.playerSettings?.let { if (!it.containsKey(player.uniqueId)) return }
        main.playerSettings?.remove(player.uniqueId)
    }
}