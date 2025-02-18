/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.xodium.vanillaplus.Utils.format
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.DimensionData
import org.xodium.vanillaplus.modules.SkinsModule

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

    private val dimsItem = ItemBuilder.from(Material.ENDER_PEARL)
        .name(Utils.mangoFormat("Dimensions").mm())
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Teleport, Explore, and Discover",
                "   <gray>Shortcut: <gold>/dims"
            ).mm()
        )
        .asGuiItem { player, _ -> dimsGUI(player).open(player) }

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

    private val besttoolsItem = ItemBuilder.from(Material.CHEST)
        .name(Utils.mangoFormat("BestTools").mm())
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
        .name(Utils.mangoFormat("Skins").mm())
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Opens the Skins Manager",
                "   <gray>Shortcut: <gold>/skins"
            ).mm()
        )
        .asGuiItem { player, _ -> SkinsModule().gui(player).open(player) }

    fun faqGUI(): Gui {
        return buildGui {
            title(Utils.firewatchFormat("FAQ").mm())
            statelessComponent {
                it.setItem(0, skillsItem)
                it.setItem(1, dimsItem)
                it.setItem(2, Utils.fillerItem)
                it.setItem(3, tipsItem)
                it.setItem(4, rulesItem)
                it.setItem(5, skinsItem)
                it.setItem(6, Utils.fillerItem)
                it.setItem(7, homesItem)
                it.setItem(8, settingsItem)
            }
        }
    }

    fun dimsGUI(player: Player): Gui {
        return buildGui {
            title(Utils.firewatchFormat("Dimensions").mm())
            statelessComponent {
                listOf(
                    DimensionData(
                        2,
                        "world",
                        "<b><gradient:#36CB2D:#3AEF55>The Overworld</gradient></b>",
                        Material.GRASS_BLOCK
                    ),
                    DimensionData(
                        4,
                        "world_nether",
                        "<b><gradient:#CB2D3E:#EF473A>The Underworld</gradient></b>",
                        Material.NETHERRACK,
                        listOf(EntityType.ELDER_GUARDIAN)
                    ),
                    DimensionData(
                        6,
                        "world_the_end",
                        "<b><gradient:#A22DCB:#EF3AEA>The Endworld</gradient></b>",
                        Material.END_STONE,
                        listOf(EntityType.WITHER, EntityType.WARDEN)
                    )
                ).forEach { data ->
                    val world = instance.server.getWorld(data.worldName) ?: return@forEach
                    val environment = world.environment.name.lowercase()
                    val worldSize = Utils.worldSizeFormat(world.worldBorder.size.toInt())
                    val difficulty = world.difficulty.name.uppercase()
                    val pvp = if (world.pvp) "<green>True" else "<red>False"
                    val hasUnlocked = DimensionData.hasUnlocked(player.uniqueId, data.requiredBossDefeated)
                    val lore = if (hasUnlocked) {
                        listOf(
                            "<dark_gray>▶ <gray>Click to teleport <dark_gray>◀",
                            "",
                            "<dark_gray>✖ <gray>Information:",
                            "   <aqua>Environment: <gold>$environment",
                            "   <aqua>Size: <gold>${worldSize} <gray>x <gold>$worldSize",
                            "   <aqua>Difficulty: <yellow>$difficulty",
                            "   <aqua>PVP: $pvp"
                        ).mm()
                    } else {
                        listOf(
                            "<dark_gray>✖ <gray>Requirements:",
                            "   <red>Defeat <gold>${data.requiredBossDefeated?.format(" <red>&<gold> ")}",
                        ).mm()
                    }
                    it.setItem(
                        data.guiIndex, ItemBuilder.from(data.itemMaterial)
                            .name(data.displayName.mm())
                            .lore(lore)
                            .asGuiItem { player, _ -> if (hasUnlocked) player.performCommand("cmi rt ${data.worldName}") })
                    it.setItem(0, Utils.fillerItem)
                    it.setItem(1, Utils.fillerItem)
                    it.setItem(3, Utils.fillerItem)
                    it.setItem(5, Utils.fillerItem)
                    it.setItem(7, Utils.fillerItem)
                    it.setItem(8, Utils.backItem)
                }
            }
        }
    }

    fun settingsGUI(): Gui {
        return buildGui {
            title(Utils.firewatchFormat("Settings").mm())
            statelessComponent {
                it.setItem(0, besttoolsItem)
                (1..7).forEach { index -> it.setItem(index, Utils.fillerItem) }
                it.setItem(8, Utils.backItem)
            }
        }
    }
}