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
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt

class QuestModule : ModuleInterface<QuestModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    private val advancementManager = advancementManager()

    init {
        if (enabled()) {
            listOf(
                "lumberjack" to AdvancementDisplay(
                    Material.OAK_LOG,
                    "<b>Lumberjack</b>".fireFmt(),
                    "Complete the quest by chopping 1000 logs".mangoFmt(),
                    AdvancementDisplay.AdvancementFrame.TASK,
                    AdvancementVisibility.ALWAYS
                ),
                "miner" to AdvancementDisplay(
                    Material.STONE_PICKAXE,
                    "<b>Miner</b>".fireFmt(),
                    "Complete the quest by mining 1000 ores".mangoFmt(),
                    AdvancementDisplay.AdvancementFrame.TASK,
                    AdvancementVisibility.ALWAYS
                )
            ).forEach { (name, display) ->
                advancementManager.addAdvancement(createAdvancement(name, display))
            }
        }
    }

    /**
     * Returns the advancement manager for this module.
     * @return The advancement manager.
     */
    private fun advancementManager(): AdvancementManager {
        return AdvancementManager(
            NameKey(
                instance::class.simpleName!!.lowercase(),
                QuestModule::class.simpleName!!.lowercase()
            ),
        )
    }

    /**
     * Creates an advancement with the given name and display.
     * @param name The name of the advancement.
     * @param display The display information for the advancement.
     * @return The created advancement.
     */
    private fun createAdvancement(name: String, display: AdvancementDisplay): Advancement {
        return Advancement(
            advancementManager.getAdvancement(
                NameKey(
                    instance::class.simpleName!!.lowercase(),
                    "${QuestModule::class.simpleName!!.lowercase()}/root"
                )
            ),
            NameKey(
                instance::class.simpleName!!.lowercase(),
                "${QuestModule::class.simpleName!!.lowercase()}/$name"
            ),
            display
        )
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