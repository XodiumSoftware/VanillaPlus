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
            // Lumberjack Advancements
            val lumberjackRoot = createAdvancement(
                null, "lumberjack_root",
                AdvancementDisplay(
                    Material.OAK_LOG, "<b>Lumberjack</b>".fireFmt(),
                    "Break a log with your bare hands".mangoFmt(), AdvancementDisplay.AdvancementFrame.TASK,
                    AdvancementVisibility.ALWAYS
                )
            )

            val lumberjack1 = createAdvancement(
                lumberjackRoot, "lumberjack_1",
                AdvancementDisplay(
                    Material.OAK_LOG, "<b>Lumberjack I</b>".fireFmt(),
                    "Chop 1,000 logs".mangoFmt(), AdvancementDisplay.AdvancementFrame.TASK,
                    AdvancementVisibility.ALWAYS
                )
            )
            val lumberjack2 = createAdvancement(
                lumberjack1, "lumberjack_2",
                AdvancementDisplay(
                    Material.SPRUCE_LOG, "<b>Lumberjack II</b>".fireFmt(),
                    "Chop 2,500 logs".mangoFmt(), AdvancementDisplay.AdvancementFrame.CHALLENGE,
                    AdvancementVisibility.ALWAYS
                )
            )
            createAdvancement(
                lumberjack2, "lumberjack_3",
                AdvancementDisplay(
                    Material.DARK_OAK_LOG, "<b>Lumberjack III</b>".fireFmt(),
                    "Chop 5,000 logs".mangoFmt(), AdvancementDisplay.AdvancementFrame.GOAL,
                    AdvancementVisibility.ALWAYS
                )
            )

            // Miner Advancement
            createAdvancement(
                null, "miner",
                AdvancementDisplay(
                    Material.STONE_PICKAXE, "<b>Miner</b>".fireFmt(),
                    "Mine 1,000 ores".mangoFmt(), AdvancementDisplay.AdvancementFrame.TASK,
                    AdvancementVisibility.ALWAYS
                )
            )
        }
    }

    /**
     * Returns the namespace for advancements in this module.
     * @return The namespace key part.
     */
    private fun namespace(): String = instance::class.simpleName!!.lowercase()

    /**
     * Returns the advancement manager for this module.
     * @return The advancement manager.
     */
    private fun advancementManager(): AdvancementManager {
        return AdvancementManager(NameKey(namespace(), QuestModule::class.simpleName!!.lowercase()))
    }

    /**
     * Creates an advancement with the given name and display.
     * @param parent The parent advancement.
     * @param name The name of the advancement.
     * @param display The display information for the advancement.
     * @return The created advancement.
     */
    private fun createAdvancement(parent: Advancement?, name: String, display: AdvancementDisplay): Advancement {
        val advancement = Advancement(parent, NameKey(namespace(), name), display)
        advancementManager.addAdvancement(advancement)
        return advancement
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