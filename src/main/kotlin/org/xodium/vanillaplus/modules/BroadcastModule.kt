/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils.mm

/**
 * A module that broadcasts a random message to all online players at regular intervals.
 */
class BroadcastModule : ModuleInterface {
    override fun enabled(): Boolean = Config.BroadcastModule.ENABLED

    init {
        if (enabled()) {
            instance.server.scheduler.scheduleSyncRepeatingTask(
                instance,
                { broadcast() },
                Config.BroadcastModule.INIT_DELAY,
                Config.BroadcastModule.INTERVAL
            )
        }
    }

    /**
     * Broadcasts a random message to all online players.
     */
    private fun broadcast() {
        instance.server.onlinePlayers.forEach {
            it.sendMessage("<gold>[<dark_aqua>TIP<gold>] ${Config.BroadcastModule.MESSAGES.random()}".mm())
        }
    }
}