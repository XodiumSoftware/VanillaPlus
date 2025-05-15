/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Represents a module handling broadcast mechanics within the system. */
class BroadcastModule : ModuleInterface {
    override fun enabled(): Boolean = Config.BroadcastModule.ENABLED

    init {
        if (enabled()) schedule()
    }

    /** Holds all the schedules for this module. */
    private fun schedule() {
        instance.server.scheduler.runTaskTimerAsynchronously(
            instance,
            Runnable { broadcast() },
            Config.BroadcastModule.INIT_DELAY,
            Config.BroadcastModule.INTERVAL
        )
    }

    /** Handles the broadcast mechanics. */
    private fun broadcast() {
        instance.server.onlinePlayers.forEach {
            it.sendMessage("${Config.BroadcastModule.MESSAGE_PREFIX} ${Config.BroadcastModule.MESSAGES.random()}".mm())
        }
    }
}