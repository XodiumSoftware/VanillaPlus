/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import dev.triumphteam.gui.item.GuiItem
import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.data.ConfigData

object Admin {

    fun item(
        material: Material,
        name: Component,
        lore: List<String>,
        action: (Player) -> Unit
    ): GuiItem<Player, ItemStack> =
        ItemBuilder.from(material).name(name).lore(lore.mm()).asGuiItem { player, _ -> action(player) }

    fun gui(): Gui = buildGui {
        spamPreventionDuration = ConfigData().guiAntiSpamDuration
        title(Utils.mangoFormat("Rules").mm())
        statelessComponent {
            it.setItem(
                0,
                item(
                    Material.WRITABLE_BOOK,
                    Utils.mangoFormat("Rules").mm(),
                    listOf(
                        "<dark_gray>▶ <gray>Click to open <dark_gray>◀",
                        "",
                        "<dark_gray>✖ <dark_aqua>All kinds of Rules & Regulations",
                        "   <gray>Shortcut: <gold>/rules"
                    )
                ) { it.performCommand("admin") }
            )
        }
    }
}