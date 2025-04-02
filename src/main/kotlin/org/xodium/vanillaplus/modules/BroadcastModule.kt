/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.audience.Audience
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.TimeUtils.minutes
import org.xodium.vanillaplus.utils.TimeUtils.seconds

class BroadcastModule : ModuleInterface {
    override fun enabled(): Boolean = Config.BroadcastModule.ENABLED

    init {
        if (enabled()) {
            instance.server.scheduler.scheduleSyncRepeatingTask(
                instance,
                { broadcast(Audience.audience()) },
                10.seconds,
                1.minutes
            )
        }
    }

    /**
     * Broadcasts a message to the specified audience.
     *
     * @param audience The audience to send the message to.
     */
    fun broadcast(audience: Audience) {
        Config.BroadcastModule.MESSAGES.shuffled().forEach { audience.sendMessage(it.mm()) }
    }
}