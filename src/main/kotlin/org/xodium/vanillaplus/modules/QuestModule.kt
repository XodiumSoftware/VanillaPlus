/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import eu.endercentral.crazy_advancements.NameKey
import eu.endercentral.crazy_advancements.advancement.Advancement
import eu.endercentral.crazy_advancements.advancement.AdvancementDisplay
import eu.endercentral.crazy_advancements.advancement.AdvancementVisibility
import eu.endercentral.crazy_advancements.manager.AdvancementManager
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface

class QuestModule : ModuleInterface<QuestModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    private val advancementManager = AdvancementManager(
        NameKey(
            instance::class.simpleName!!.lowercase(),
            QuestModule::class.simpleName!!.lowercase()
        ),
    )

    init {
        if (enabled()) {
            val rootDisplay = AdvancementDisplay(
                Material.GRASS_BLOCK,
                "Quests",
                "All your quests in one place",
                AdvancementDisplay.AdvancementFrame.TASK,
                AdvancementVisibility.ALWAYS
            ).apply {
                background(NameKey("minecraft", "block/red_terracotta.png"))
            }
            val rootAdvancement = Advancement(
                null,
                NameKey(
                    instance::class.simpleName!!.lowercase(),
                    "${QuestModule::class.simpleName!!.lowercase()}/root"
                ),
                rootDisplay
            )
            advancementManager.addAdvancement(rootAdvancement)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return
        advancementManager.addPlayer(event.player)
    }

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}