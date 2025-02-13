/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import org.bukkit.Material
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.DimensionData
import org.xodium.vanillaplus.modules.SkinsModule

object Gui {
    private val skillsItem = ItemBuilder.from(Material.NETHERITE_SWORD)
        .name(Utils.mangoFormat("Skills"))
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Leveling, Skills, and Abilities",
                "   <gray>Shortcut: <gold>/skills"
            ).mm()
        )
        .asGuiItem { player, _ -> player.performCommand("skills") }

    private val dimsItem = ItemBuilder.from(Material.ENDER_PEARL)
        .name(Utils.mangoFormat("Dimensions"))
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Teleport, Explore, and Discover",
                "   <gray>Shortcut: <gold>/dims"
            ).mm()
        )
        .asGuiItem { player, _ -> dimsGUI().open(player) }

    private val tipsItem = ItemBuilder.from(Material.LIGHT)
        .name(Utils.mangoFormat("Tips"))
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>All kinds of Tips & Tricks",
                "   <gray>Shortcut: <gold>/tips"
            ).mm()
        )
        .asGuiItem { player, _ -> player.performCommand("tips") }

    private val rulesItem = ItemBuilder.from(Material.WRITABLE_BOOK)
        .name(Utils.mangoFormat("Rules"))
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>The Rules of this server",
                "   <gray>Shortcut: <gold>/rules"
            ).mm()
        )
        .asGuiItem { player, _ -> player.performCommand("rules") }

    private val homesItem = ItemBuilder.from(Material.RED_BED)
        .name(Utils.mangoFormat("Homes"))
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Opens the Homes Settings",
                "   <gray>Shortcut: <gold>/homes"
            ).mm()
        )
        .asGuiItem { player, _ -> player.performCommand("homes") }

    private val settingsItem = ItemBuilder.from(Material.CLOCK)
        .name(Utils.mangoFormat("Settings"))
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Personal Settings and Preferences",
                "   <gray>Shortcut: <gold>/settings"
            ).mm()
        )
        .asGuiItem { player, _ -> settingsGUI().open(player) }

    private val besttoolsItem = ItemBuilder.from(Material.CHEST)
        .name(Utils.mangoFormat("BestTools"))
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Opens the ChestSort Settings",
                "   <gray>Shortcut: <gold>/sort"
            ).mm()
        )
        .asGuiItem { player, _ -> player.performCommand("sort") }

    private val skinsItem = ItemBuilder.from(Material.WITHER_SKELETON_SKULL)
        .name(Utils.mangoFormat("Skins"))
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Opens the Skins Manager",
                "   <gray>Shortcut: <gold>/skins"
            ).mm()
        )
        .asGuiItem { player, _ -> SkinsModule().gui().open(player) }

    fun faqGUI(): Gui {
        return buildGui {
            title(Utils.birdflopFormat("FAQ"))
            statelessComponent { gui ->
                gui.setItem(0, skillsItem)
                gui.setItem(1, dimsItem)
                gui.setItem(2, Utils.fillerItem)
                gui.setItem(3, tipsItem)
                gui.setItem(4, rulesItem)
                gui.setItem(5, skinsItem)
                gui.setItem(6, Utils.fillerItem)
                gui.setItem(7, homesItem)
                gui.setItem(8, settingsItem)
            }
        }
    }

    fun dimsGUI(): Gui {
        return buildGui {
            title(Utils.birdflopFormat("Dimensions"))
            statelessComponent { gui ->
                listOf(
                    DimensionData(2, "world", "<green><bold>The Overworld", Material.GRASS_BLOCK),
                    DimensionData(4, "world_nether", "<red><bold>The Underworld", Material.NETHERRACK),
                    DimensionData(6, "world_the_end", "<dark_purple><bold>The Endworld", Material.END_STONE)
                ).forEach { data ->
                    val world = instance.server.getWorld(data.worldName) ?: return@forEach
                    val environment = world.environment.name.lowercase()
                    val worldSize = Utils.worldSizeFormat(world.worldBorder.size.toInt())
                    val difficulty = world.difficulty.name.uppercase()
                    val pvp = if (world.pvp) "<green>True" else "<red>False"
                    gui.setItem(
                        data.guiIndex, ItemBuilder.from(data.itemMaterial)
                            .name(data.displayName.mm())
                            .lore(
                                listOf(
                                    "<dark_gray>▶ <gray>Click to rtp <dark_gray>◀",
                                    "",
                                    "<dark_gray>✖ <gray>Information:",
                                    "   <aqua>Environment: <gold>$environment",
                                    "   <aqua>Size: <gold>${worldSize} <gray>x <gold>$worldSize",
                                    "   <aqua>Difficulty: <yellow>$difficulty",
                                    "   <aqua>PVP: $pvp"
                                ).mm()
                            ).asGuiItem { player, _ -> player.performCommand("cmi rt ${data.worldName}") })
                    gui.setItem(0, Utils.fillerItem)
                    gui.setItem(1, Utils.fillerItem)
                    gui.setItem(3, Utils.fillerItem)
                    gui.setItem(5, Utils.fillerItem)
                    gui.setItem(7, Utils.fillerItem)
                    gui.setItem(8, Utils.backItem)
                }
            }
        }
    }

    fun settingsGUI(): Gui {
        return buildGui {
            title(Utils.birdflopFormat("Settings"))
            statelessComponent { gui ->
                gui.setItem(0, besttoolsItem)
                (1..7).forEach { index -> gui.setItem(index, Utils.fillerItem) }
                gui.setItem(8, Utils.backItem)
            }
        }
    }
}