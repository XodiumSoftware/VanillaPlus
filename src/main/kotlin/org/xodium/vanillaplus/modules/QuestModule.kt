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
            lumberjack()
            miner()
        }
    }

    private fun lumberjack() {
        val lumberjackRoot = createAdvancement(
            null, "lumberjack_root",
            AdvancementDisplay(
                Material.STICK, "<b>Lumberjack</b>".fireFmt(),
                """
                Requirement: Break a log with your bare hands
                Reward: 1x Bottle o' Enchanting
                """.trimIndent().mangoFmt(),
                AdvancementDisplay.AdvancementFrame.TASK,
                AdvancementVisibility.ALWAYS
            )
        ).apply { display.x = 0f }

        val lumberjack1 = createAdvancement(
            lumberjackRoot, "lumberjack_1",
            AdvancementDisplay(
                Material.OAK_LOG, "<b>Lumberjack I</b>".fireFmt(),
                """
                Requirement: Chop 1k logs
                Reward: 5x Bottles o' Enchanting
                """.trimIndent().mangoFmt(),
                AdvancementDisplay.AdvancementFrame.TASK,
                AdvancementVisibility.ALWAYS
            )
        ).apply { display.x = 1f }

        val lumberjack2 = createAdvancement(
            lumberjack1, "lumberjack_2",
            AdvancementDisplay(
                Material.SPRUCE_LOG, "<b>Lumberjack II</b>".fireFmt(),
                """
                Requirement: Chop 2.5k logs
                Reward: 10x Bottles o' Enchanting
                """.trimIndent().mangoFmt(),
                AdvancementDisplay.AdvancementFrame.TASK,
                AdvancementVisibility.ALWAYS
            )
        ).apply { display.x = 2f }

        val lumberjack3 = createAdvancement(
            lumberjack2, "lumberjack_3",
            AdvancementDisplay(
                Material.DARK_OAK_LOG, "<b>Lumberjack III</b>".fireFmt(),
                """
                Requirement: Chop 5k logs
                Reward: 15x Bottles o' Enchanting
                """.trimIndent().mangoFmt(),
                AdvancementDisplay.AdvancementFrame.TASK,
                AdvancementVisibility.ALWAYS
            )
        ).apply { display.x = 3f }

        val lumberjack4 = createAdvancement(
            lumberjack3, "lumberjack_4",
            AdvancementDisplay(
                Material.BIRCH_LOG, "<b>Lumberjack IV</b>".fireFmt(),
                """
                Requirement: Chop 10k logs
                Reward: 20x Bottles o' Enchanting
                """.trimIndent().mangoFmt(),
                AdvancementDisplay.AdvancementFrame.TASK,
                AdvancementVisibility.ALWAYS
            )
        ).apply { display.x = 4f }

        val lumberjack5 = createAdvancement(
            lumberjack4, "lumberjack_5",
            AdvancementDisplay(
                Material.JUNGLE_LOG, "<b>Lumberjack V</b>".fireFmt(),
                """
                Requirement: Chop 25k logs
                Reward: 25x Bottles o' Enchanting
                """.trimIndent().mangoFmt(),
                AdvancementDisplay.AdvancementFrame.TASK,
                AdvancementVisibility.ALWAYS
            )
        ).apply { display.x = 5f }

        val lumberjack6 = createAdvancement(
            lumberjack5, "lumberjack_6",
            AdvancementDisplay(
                Material.ACACIA_LOG, "<b>Lumberjack VI</b>".fireFmt(),
                """
                Requirement: Chop 50k logs
                Reward: 30x Bottles o' Enchanting
                """.trimIndent().mangoFmt(),
                AdvancementDisplay.AdvancementFrame.TASK,
                AdvancementVisibility.ALWAYS
            )
        ).apply { display.x = 6f }

        val lumberjack7 = createAdvancement(
            lumberjack6, "lumberjack_7",
            AdvancementDisplay(
                Material.CHERRY_LOG, "<b>Lumberjack VII</b>".fireFmt(),
                """
                Requirement: Chop 100k logs
                Reward: 40x Bottles o' Enchanting
                """.trimIndent().mangoFmt(),
                AdvancementDisplay.AdvancementFrame.TASK,
                AdvancementVisibility.ALWAYS
            )
        ).apply { display.x = 7f }

        val lumberjack8 = createAdvancement(
            lumberjack7, "lumberjack_8",
            AdvancementDisplay(
                Material.MANGROVE_LOG, "<b>Lumberjack VIII</b>".fireFmt(),
                """
                Requirement: Chop 250k logs
                Reward: 50x Bottles o' Enchanting
                """.trimIndent().mangoFmt(),
                AdvancementDisplay.AdvancementFrame.TASK,
                AdvancementVisibility.ALWAYS
            )
        ).apply { display.x = 8f }

        val lumberjack9 = createAdvancement(
            lumberjack8, "lumberjack_9",
            AdvancementDisplay(
                Material.CRIMSON_STEM, "<b>Lumberjack IX</b>".fireFmt(),
                """
                Requirement: Chop 500k logs
                Reward: 64x Bottles o' Enchanting
                """.trimIndent().mangoFmt(),
                AdvancementDisplay.AdvancementFrame.TASK,
                AdvancementVisibility.ALWAYS
            )
        ).apply { display.x = 9f }

        createAdvancement(
            lumberjack9, "lumberjack_10",
            AdvancementDisplay(
                Material.WARPED_STEM, "<b>Lumberjack X</b>".fireFmt(),
                """
                Requirement: Chop 1m logs
                Reward: 2x 64x Bottles o' Enchanting
                """.trimIndent().mangoFmt(),
                AdvancementDisplay.AdvancementFrame.TASK,
                AdvancementVisibility.ALWAYS
            )
        ).apply { display.x = 10f }
    }

    private fun miner() {
        createAdvancement(
            null, "miner",
            AdvancementDisplay(
                Material.STONE_PICKAXE, "<b>Miner</b>".fireFmt(),
                "Mine 1k ores".mangoFmt(),
                AdvancementDisplay.AdvancementFrame.TASK,
                AdvancementVisibility.ALWAYS
            )
        ).apply { display.x = 0f }
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