/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface

class QuestModule : ModuleInterface<QuestModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    private val advancementAPI = UltimateAdvancementAPI.getInstance(instance)
    private val advancementTab = advancementAPI.createAdvancementTab("vanillaplus")
    private val advancementDisplay =
        AdvancementDisplay(Material.GRASS_BLOCK, "VanillaPlus", AdvancementFrameType.TASK, true, true, 0f, 0f, "")
    private val advancementRoot =
        RootAdvancement(advancementTab, "root", advancementDisplay, "textures/block/stone.png")

    init {
        if (enabled()) advancementTab.registerAdvancements(advancementRoot)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerLoadingCompletedEvent) {
        if (!enabled()) return
        advancementTab.showTab(event.player)
    }

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}