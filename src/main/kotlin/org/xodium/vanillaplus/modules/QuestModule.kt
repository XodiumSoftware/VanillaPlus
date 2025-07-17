package org.xodium.vanillaplus.modules

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType
import com.fren_gor.ultimateAdvancementAPI.events.PlayerLoadingCompletedEvent
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling quest mechanics within the system. */
class QuestModule : ModuleInterface<QuestModule.Config> {
    override val config: Config = Config()

    private lateinit var advancementApi: UltimateAdvancementAPI

    override fun enabled(): Boolean = config.enabled

    init {
        if (enabled()) advancementApi = UltimateAdvancementAPI.getInstance(instance)
    }

    @EventHandler
    fun on(event: PlayerLoadingCompletedEvent) {
        if (!enabled()) return

        advancements().showTab(event.player)
    }

    private fun advancements(): AdvancementTab {
        val advancementTab = advancementApi.createAdvancementTab("TEST")
        val advancementDisplay =
            AdvancementDisplay(Material.GRASS_BLOCK, "Test1", AdvancementFrameType.TASK, true, true, 0f, 0f, "desc")
        val rootAdvancement = RootAdvancement(advancementTab, "root", advancementDisplay, "textures/block/stone.png")
        advancementTab.registerAdvancements(rootAdvancement)
        return advancementTab
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}