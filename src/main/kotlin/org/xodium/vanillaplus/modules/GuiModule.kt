/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.container.GuiContainer
import dev.triumphteam.gui.item.GuiItem
import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.container.type.HopperContainerType
import dev.triumphteam.gui.paper.container.type.PaperContainerType
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.DimensionData
import org.xodium.vanillaplus.interfaces.ModuleInterface


//TODO: refactor
//FIX: ModelData not working
class GuiModule : ModuleInterface {
    private val antiSpamDuration = Config.GuiModule.ANTI_SPAM_DURATION

    private fun birdflopFormat(p0: String): Component = "<b><gradient:#CB2D3E:#EF473A>$p0</gradient></b>".mm()
    private fun mangoFormat(p0: String): Component = "<b><gradient:#FFE259:#FFA751>$p0</gradient></b>".mm()
    private fun worldSizeFormat(p0: Int): String = if (p0 >= 1000) "${p0 / 1000}k" else p0.toString()

    fun faqGUI(): Gui {
        return buildGui {
            spamPreventionDuration = antiSpamDuration
            title(birdflopFormat("FAQ"))
            statelessComponent { gui ->
                filler(gui, 0..8)
                gui.setItem(
                    0, ItemBuilder.from(Material.NETHERITE_SWORD)
                        .name(mangoFormat("Skills"))
                        .lore(
                            listOf(
                                "<dark_gray>▶ <gray>Click the item to open <dark_gray>◀",
                                "",
                                "<dark_gray>✖ <dark_aqua>Leveling, Skills, and Abilities",
                                "   <gray>Shortcut: <gold>/skills"
                            ).mm()
                        )
                        .asGuiItem { player, _ -> player.performCommand("skills") })
                gui.setItem(
                    1, ItemBuilder.from(Material.BLAZE_ROD)
                        .name(mangoFormat("Dimensions"))
                        .lore(
                            listOf(
                                "<dark_gray>▶ <gray>Click the item to open <dark_gray>◀",
                                "",
                                "<dark_gray>✖ <dark_aqua>Teleport, Explore, and Discover",
                                "   <gray>Shortcut: <gold>/dims"
                            ).mm()
                        )
                        .model(2)
                        .asGuiItem { player, _ -> dimsGUI().open(player) })
                gui.setItem(
                    3, ItemBuilder.from(Material.LIGHT)
                        .name(mangoFormat("Tips"))
                        .lore(
                            listOf(
                                "<dark_gray>▶ <gray>Click the item to open <dark_gray>◀",
                                "",
                                "<dark_gray>✖ <dark_aqua>All kinds of Tips & Tricks",
                                "   <gray>Shortcut: <gold>/tips"
                            ).mm()
                        )
                        .asGuiItem { player, _ -> player.performCommand("tips") })
                gui.setItem(
                    4, ItemBuilder.from(Material.BLAZE_ROD)
                        .name(mangoFormat("Rules"))
                        .lore(
                            listOf(
                                "<dark_gray>▶ <gray>Click the item to open <dark_gray>◀",
                                "",
                                "<dark_gray>✖ <dark_aqua>The Rules of this server",
                                "   <gray>Shortcut: <gold>/rules"
                            ).mm()
                        )
                        .model(1)
                        .asGuiItem { player, _ -> player.performCommand("rules") })
                gui.setItem(
                    7, ItemBuilder.from(Material.RED_BED)
                        .name(mangoFormat("Homes"))
                        .lore(
                            listOf(
                                "<dark_gray>▶ <gray>Click the item to open <dark_gray>◀",
                                "",
                                "<dark_gray>✖ <dark_aqua>Opens the Homes Settings",
                                "   <gray>Shortcut: <gold>/homes"
                            ).mm()
                        )
                        .asGuiItem { player, _ -> player.performCommand("homes") })
                gui.setItem(
                    8, ItemBuilder.from(Material.BLAZE_ROD)
                        .name(mangoFormat("Settings"))
                        .lore(
                            listOf(
                                "<dark_gray>▶ <gray>Click the item to open <dark_gray>◀",
                                "",
                                "<dark_gray>✖ <dark_aqua>Personal Settings and Preferences",
                                "   <gray>Shortcut: <gold>/settings"
                            ).mm()
                        )
                        .model(3)
                        .asGuiItem { player, _ -> settingsGUI().open(player) })
            }
        }
    }

    fun dimsGUI(): Gui {
        return buildGui {
            containerType = HopperContainerType.of() as? PaperContainerType
            spamPreventionDuration = antiSpamDuration
            title(birdflopFormat("Dimensions"))
            statelessComponent { gui ->
                filler(gui, 0..4)
                listOf(
                    DimensionData(0, "world", "<green><bold>The Overworld", 4),
                    DimensionData(1, "world_nether", "<red><bold>The Underworld", 5),
                    DimensionData(2, "world_the_end", "<dark_purple><bold>The Endworld", 6)
                ).forEach { data ->
                    val world = instance.server.getWorld(data.worldName) ?: return@forEach
                    val environment = world.environment.name.lowercase()
                    val worldSize = worldSizeFormat(world.worldBorder.size.toInt())
                    val difficulty = world.difficulty.name.uppercase()
                    val pvp = if (world.pvp) "<green>True" else "<red>False"
                    gui.setItem(
                        data.guiIndex, ItemBuilder.from(Material.BLAZE_ROD)
                            .name(data.displayName.mm())
                            .lore(
                                listOf(
                                    "<dark_gray>▶ <gray>Click the item to rtp <dark_gray>◀",
                                    "",
                                    "<dark_gray>✖ <gray>Information:",
                                    "   <aqua>Environment: <gold>$environment",
                                    "   <aqua>Size: <gold>${worldSize} <gray>x <gold>$worldSize",
                                    "   <aqua>Difficulty: <yellow>$difficulty",
                                    "   <aqua>PVP: $pvp"
                                ).mm()
                            )
                            .model(data.itemModelNumber)
                            .asGuiItem { player, _ -> player.performCommand("cmi rt ${data.worldName}") })
                    gui.setItem(4, backButton())
                }
            }
        }
    }

    fun settingsGUI(): Gui {
        return buildGui {
            containerType = HopperContainerType.of() as? PaperContainerType
            spamPreventionDuration = antiSpamDuration
            title(birdflopFormat("Settings"))
            statelessComponent { gui ->
                filler(gui, 0..4)
                gui.setItem(
                    0, ItemBuilder.from(Material.CHEST)
                        .name(mangoFormat("BestTools"))
                        .lore(
                            listOf(
                                "<dark_gray>▶ <gray>Click the item to open <dark_gray>◀",
                                "",
                                "<dark_gray>✖ <dark_aqua>Opens the ChestSort Settings",
                                "   <gray>Shortcut: <gold>/sort"
                            ).mm()
                        )
                        .asGuiItem { player, _ -> player.performCommand("sort") })
                gui.setItem(4, backButton())
            }
        }
    }

    private fun backButton(): GuiItem<Player, ItemStack> {
        return ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
            .name(birdflopFormat("Back"))
            .lore(listOf("<dark_gray>✖ <gray>Return to the previous menu").mm())
            .asGuiItem { player, _ -> faqGUI().open(player) }
    }

    private fun filler(gui: GuiContainer<Player, ItemStack>, range: IntRange) {
        for (slot in range) gui.setItem(
            slot, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
                .name("".mm())
                .asGuiItem()
        )
    }

    override fun enabled(): Boolean = Config.GuiModule.ENABLED
}