/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.audience.Audience
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.PREFIX
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface

class BroadcastModule : ModuleInterface {
    override fun enabled(): Boolean = Config.BroadcastModule.ENABLED

    init {
        if (enabled()) {
            instance.server.scheduler.scheduleSyncRepeatingTask(
                instance,
                { broadcast(Audience.audience()) },
                Config.BroadcastModule.INIT_DELAY,
                Config.BroadcastModule.INTERVAL
            )
        }
    }

    /**
     * Broadcasts a message to the specified audience.
     *
     * @param audience The audience to send the message to.
     */
    fun broadcast(audience: Audience) {
        Config.BroadcastModule.MESSAGES.shuffled().forEach { audience.sendMessage("$PREFIX + $it".mm()) }
    }
}