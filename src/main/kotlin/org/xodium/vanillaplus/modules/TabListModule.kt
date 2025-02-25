/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.audience.Audience
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface

class TabListModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.TabListModule().enabled

    init {
        instance.server.scheduler.runTaskTimer(instance, Runnable {
            instance.server.onlinePlayers.forEach { player: Audience ->
                player.sendPlayerListHeaderAndFooter(
                    ConfigData.TabListModule().header.mm(),
                    ConfigData.TabListModule().footer.mm()
                )
            }
        }, ConfigData.TabListModule().startDelay, ConfigData.TabListModule().period)
    }
}