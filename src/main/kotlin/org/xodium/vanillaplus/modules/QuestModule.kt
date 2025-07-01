/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.xodium.vanillaplus.interfaces.ModuleInterface

class QuestModule : ModuleInterface<QuestModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    init {
        if (enabled())
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerLoadingCompletedEvent) {
        if (!enabled()) return
    }

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}