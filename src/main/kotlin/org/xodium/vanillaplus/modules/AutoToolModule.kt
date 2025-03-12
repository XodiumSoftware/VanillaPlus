/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface

class AutoToolModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.AutoToolModule().enabled

    @EventHandler
    fun on(event: PlayerQuitEvent): Unit = TODO("check database if enabled")

    val guiItem = ItemBuilder.from(Material.MILK_BUCKET)
        .name(Utils.mangoFormat("AutoTool").mm())
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to toggle <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Auto switch to best tool",
                "   <gray>Shortcut: <gold>/autotool | /at"
            ).mm()
        )
        .asGuiItem { player, _ -> toggle(player) }

    fun toggle(player: Player) {
        TODO("toggle in database")
    }
}