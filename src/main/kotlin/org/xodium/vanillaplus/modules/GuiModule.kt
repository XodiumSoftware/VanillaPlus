/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.container.type.HopperContainerType
import dev.triumphteam.gui.paper.container.type.PaperContainerType
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.DimensionData
import org.xodium.vanillaplus.interfaces.ModuleInterface

class GuiModule : ModuleInterface {
    override fun enabled(): Boolean = Config.GuiModule.ENABLED

    private val antiSpamDuration = Config.GuiModule.ANTI_SPAM_DURATION

    private fun birdflopFormat(text: String): Component = "<b><gradient:#CB2D3E:#EF473A>$text</gradient></b>".mm()
    private fun mangoFormat(text: String): Component = "<b><gradient:#FFE259:#FFA751>$text</gradient></b>".mm()
    private fun worldSizeFormat(size: Int): String = if (size >= 1000) "${size / 1000}k" else size.toString()

    private val skillsItem = ItemBuilder.from(Material.NETHERITE_SWORD)
        .name(mangoFormat("Skills"))
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click the item to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Leveling, Skills, and Abilities",
                "   <gray>Shortcut: <gold>/skills"
            ).mm()
        )
        .asGuiItem { player, _ -> player.performCommand("skills") }

    private val dimsItem = ItemBuilder.from(Material.ENDER_PEARL)
        .name(mangoFormat("Dimensions"))
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click the item to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Teleport, Explore, and Discover",
                "   <gray>Shortcut: <gold>/dims"
            ).mm()
        )
        .asGuiItem { player, _ -> dimsGUI().open(player) }

    private val tipsItem = ItemBuilder.from(Material.LIGHT)
        .name(mangoFormat("Tips"))
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click the item to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>All kinds of Tips & Tricks",
                "   <gray>Shortcut: <gold>/tips"
            ).mm()
        )
        .asGuiItem { player, _ -> player.performCommand("tips") }

    private val rulesItem = ItemBuilder.from(Material.WRITABLE_BOOK)
        .name(mangoFormat("Rules"))
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click the item to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>The Rules of this server",
                "   <gray>Shortcut: <gold>/rules"
            ).mm()
        )
        .asGuiItem { player, _ -> player.performCommand("rules") }

    private val homesItem = ItemBuilder.from(Material.RED_BED)
        .name(mangoFormat("Homes"))
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click the item to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Opens the Homes Settings",
                "   <gray>Shortcut: <gold>/homes"
            ).mm()
        )
        .asGuiItem { player, _ -> player.performCommand("homes") }

    private val settingsItem = ItemBuilder.from(Material.CLOCK)
        .name(mangoFormat("Settings"))
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click the item to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Personal Settings and Preferences",
                "   <gray>Shortcut: <gold>/settings"
            ).mm()
        )
        .asGuiItem { player, _ -> settingsGUI().open(player) }

    private val besttoolsItem = ItemBuilder.from(Material.CHEST)
        .name(mangoFormat("BestTools"))
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click the item to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Opens the ChestSort Settings",
                "   <gray>Shortcut: <gold>/sort"
            ).mm()
        )
        .asGuiItem { player, _ -> player.performCommand("sort") }

    private val backItem = ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
        .name(birdflopFormat("Back"))
        .lore(listOf("<dark_gray>✖ <gray>Return to the previous menu").mm())
        .asGuiItem { player, _ -> faqGUI().open(player) }

    private val fillerItem = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
        .name("".mm()).asGuiItem()

    fun faqGUI(): Gui {
        return buildGui {
            spamPreventionDuration = antiSpamDuration
            title(birdflopFormat("FAQ"))
            statelessComponent { gui ->
                gui.setItem(0, skillsItem)
                gui.setItem(1, dimsItem)
                gui.setItem(2, fillerItem)
                gui.setItem(3, tipsItem)
                gui.setItem(4, rulesItem)
                gui.setItem(5, fillerItem)
                gui.setItem(6, fillerItem)
                gui.setItem(7, homesItem)
                gui.setItem(8, settingsItem)
            }
        }
    }

    fun dimsGUI(): Gui {
        return buildGui {
            containerType = HopperContainerType.of() as? PaperContainerType
            spamPreventionDuration = antiSpamDuration
            title(birdflopFormat("Dimensions"))
            statelessComponent { gui ->
                listOf(
                    DimensionData(0, "world", "<green><bold>The Overworld", Material.GRASS_BLOCK),
                    DimensionData(1, "world_nether", "<red><bold>The Underworld", Material.NETHERRACK),
                    DimensionData(2, "world_the_end", "<dark_purple><bold>The Endworld", Material.END_STONE)
                ).forEach { data ->
                    val world = instance.server.getWorld(data.worldName) ?: return@forEach
                    val environment = world.environment.name.lowercase()
                    val worldSize = worldSizeFormat(world.worldBorder.size.toInt())
                    val difficulty = world.difficulty.name.uppercase()
                    val pvp = if (world.pvp) "<green>True" else "<red>False"
                    gui.setItem(
                        data.guiIndex, ItemBuilder.from(data.itemMaterial)
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
                            ).asGuiItem { player, _ -> player.performCommand("cmi rt ${data.worldName}") })
                    gui.setItem(3, fillerItem)
                    gui.setItem(4, backItem)
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
                gui.setItem(0, besttoolsItem)
                gui.setItem(1, fillerItem)
                gui.setItem(2, fillerItem)
                gui.setItem(3, fillerItem)
                gui.setItem(4, backItem)
            }
        }
    }
}