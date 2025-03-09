/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.audience.Audience
import org.bukkit.entity.Player
import org.xodium.vanillaplus.Utils.replacePlaceholders
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface

class TabListModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.TabListModule().enabled

    init {
        instance.server.scheduler.runTaskTimer(instance, Runnable {
            instance.server.onlinePlayers.forEach { audience: Audience ->
                val player = audience as Player
                audience.sendPlayerListHeaderAndFooter(
                    replacePlaceholders(ConfigData.TabListModule().header, player),
                    replacePlaceholders(ConfigData.TabListModule().footer, player)
                )
            }
        }, ConfigData.TabListModule().startDelay, ConfigData.TabListModule().period)
    }
}