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
import org.xodium.vanillaplus.data.Tier
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt

class QuestModule : ModuleInterface<QuestModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    private val advancementManager = advancementManager()

    init {
        if (enabled()) lumberjack()
    }

    private fun lumberjack() {
        createAdvancementLines(
            createAdvancement(
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
            ),
            listOf(
                listOf(
                    Tier(Material.OAK_LOG, "Lumberjack", "Chop 1k logs", "5x Bottles o' Enchanting"),
                    Tier(Material.SPRUCE_LOG, "Lumberjack", "Chop 2.5k logs", "10x Bottles o' Enchanting"),
                    Tier(Material.DARK_OAK_LOG, "Lumberjack", "Chop 5k logs", "15x Bottles o' Enchanting"),
                    Tier(Material.BIRCH_LOG, "Lumberjack", "Chop 10k logs", "20x Bottles o' Enchanting"),
                    Tier(Material.JUNGLE_LOG, "Lumberjack", "Chop 25k logs", "25x Bottles o' Enchanting"),
                    Tier(Material.ACACIA_LOG, "Lumberjack", "Chop 50k logs", "30x Bottles o' Enchanting"),
                    Tier(Material.CHERRY_LOG, "Lumberjack", "Chop 100k logs", "40x Bottles o' Enchanting"),
                    Tier(Material.MANGROVE_LOG, "Lumberjack", "Chop 250k logs", "50x Bottles o' Enchanting"),
                    Tier(Material.CRIMSON_STEM, "Lumberjack", "Chop 500k logs", "64x Bottles o' Enchanting"),
                    Tier(Material.WARPED_STEM, "Lumberjack", "Chop 1m logs", "2x 64x Bottles o' Enchanting")
                ),
                listOf(
                    Tier(Material.WOODEN_AXE, "Axes", "Craft a wooden axe", "1x Bottle o' Enchanting"),
                    Tier(Material.STONE_AXE, "Axes", "Craft a stone axe", "2x Bottles o' Enchanting"),
                    Tier(Material.IRON_AXE, "Axes", "Craft an iron axe", "5x Bottles o' Enchanting"),
                    Tier(Material.GOLDEN_AXE, "Axes", "Craft a golden axe", "3x Bottles o' Enchanting"),
                    Tier(Material.DIAMOND_AXE, "Axes", "Craft a diamond axe", "10x Bottles o' Enchanting"),
                    Tier(Material.NETHERITE_AXE, "Axes", "Craft a netherite axe", "20x Bottles o' Enchanting")
                )
            )
        )
    }

    /**
     * Creates advancement lines based on the provided root advancement and tier lists.
     * Each line corresponds to a set of tiers, creating a structured progression.
     * @param root The root advancement to attach the lines to.
     * @param lines A list of lists, where each inner list contains Tiers for that line.
     */
    private fun createAdvancementLines(root: Advancement, lines: List<List<Tier>>) {
        lines.forEachIndexed { y, line ->
            var parent = root
            line.forEachIndexed { x, tier ->
                val level = x + 1
                val romanNumeral = toRoman(level)
                val description = "Requirement: ${tier.requirement}\nReward: ${tier.reward}"

                val advancement = createAdvancement(
                    parent, "${tier.title.lowercase()}_$level",
                    AdvancementDisplay(
                        tier.icon, "<b>${tier.title} $romanNumeral</b>".fireFmt(),
                        description.mangoFmt(),
                        AdvancementDisplay.AdvancementFrame.TASK,
                        AdvancementVisibility.ALWAYS
                    )
                ).apply {
                    display.x = level.toFloat()
                    display.y = y.toFloat()
                }
                parent = advancement
            }
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

    /**
     * Converts an integer to a Roman numeral string.
     * @param number The integer to convert.
     * @return The Roman numeral representation of the integer.
     */
    private fun toRoman(number: Int): String {
        val romanValues = listOf(10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I")
        var num = number
        val roman = StringBuilder()
        while (num > 0) {
            for ((value, symbol) in romanValues) {
                if (num >= value) {
                    roman.append(symbol)
                    num -= value
                    break
                }
            }
        }
        return roman.toString()
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