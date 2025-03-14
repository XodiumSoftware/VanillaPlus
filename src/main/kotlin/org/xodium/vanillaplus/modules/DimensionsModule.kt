/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.title.Title
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDeathEvent
import org.xodium.vanillaplus.Database
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.format
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.data.DimensionData
import org.xodium.vanillaplus.interfaces.ModuleInterface


class DimensionsModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.DimensionsModule().enabled

    init {
        Database.createTable(this::class)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: EntityDeathEvent) {
        val entities = listOf<EntityType>(
            EntityType.ELDER_GUARDIAN,
            EntityType.WARDEN,
            EntityType.WITHER
        )
        if (!entities.contains(event.entityType)) return
        val killer = event.entity.killer ?: return
        if (!DimensionData.hasUnlocked(killer.uniqueId, entities)) {
            DimensionData.setUnlocked(killer.uniqueId, entities)
            killer.playSound(ConfigData.DimensionsModule().soundUnlockSkin, Sound.Emitter.self())
            killer.showTitle(
                Title.title(
                    Utils.firewatchFormat("Congratulations!").mm(),
                    Utils.mangoFormat("You have unlocked the next dimension!")
                        .mm()
                )
            )
        }
    }

    val guiItem = ItemBuilder.from(Material.ENDER_PEARL)
        .name(Utils.mangoFormat("Dimensions").mm())
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to open <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Teleport, Explore, and Discover",
                "   <gray>Shortcut: <gold>/dims"
            ).mm()
        )
        .asGuiItem { player, _ -> gui(player).open(player) }

    fun gui(player: Player): Gui {
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
}