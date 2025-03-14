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
import org.xodium.vanillaplus.modules.AutoRefillModule
import org.xodium.vanillaplus.modules.AutoToolModule
import org.xodium.vanillaplus.modules.DimensionsModule

object Gui {
    private val skillsItem = ItemBuilder.from(Material.NETHERITE_SWORD)
        .name(Utils.mangoFormat("Skills").mm())
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Leveling, Skills, and Abilities",
                "   <gray>Shortcut: <gold>/skills"
            ).mm()
        )
        .asGuiItem { player, _ -> player.performCommand("skills") }

    private val tipsItem = ItemBuilder.from(Material.LIGHT)
        .name(Utils.mangoFormat("Tips").mm())
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
        .name(Utils.mangoFormat("Rules").mm())
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
        .name(Utils.mangoFormat("Homes").mm())
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
        .name(Utils.mangoFormat("Settings").mm())
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Personal Settings and Preferences",
                "   <gray>Shortcut: <gold>/settings"
            ).mm()
        )
        .asGuiItem { player, _ -> settingsGUI().open(player) }

    private val chestsortItem = ItemBuilder.from(Material.CHEST)
        .name(Utils.mangoFormat("ChestSort").mm())
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Opens the ChestSort Settings",
                "   <gray>Shortcut: <gold>/sort"
            ).mm()
        )
        .asGuiItem { player, _ -> player.performCommand("sort") }

    fun faqGUI(): Gui {
        return buildGui {
            title(Utils.firewatchFormat("FAQ").mm())
            statelessComponent {
                it.setItem(0, skillsItem)
                it.setItem(1, DimensionsModule().guiItem)
                it.setItem(2, Utils.fillerItem)
                it.setItem(3, tipsItem)
                it.setItem(4, rulesItem)
                it.setItem(5, Utils.fillerItem)
                it.setItem(6, Utils.fillerItem)
                it.setItem(7, homesItem)
                it.setItem(8, settingsItem)
            }
        }
    }

    fun settingsGUI(): Gui {
        return buildGui {
            title(Utils.firewatchFormat("Settings").mm())
            statelessComponent {
                it.setItem(0, chestsortItem)
                it.setItem(1, AutoToolModule().guiItem)
                it.setItem(2, AutoRefillModule().guiItem)
                (3..7).forEach { index -> it.setItem(index, Utils.fillerItem) }
                it.setItem(8, Utils.backItem)
            }
        }
    }
}