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

    /**
     * Create a GUI item.
     *
     * @param material The material of the item.
     * @param name The name of the item.
     * @param lore The lore of the item.
     * @param action The action to perform when the item is clicked.
     * @return The GUI item.
     */
    fun item( //TODO: move builder to Utils.
        material: Material,
        name: Component,
        lore: List<String>,
        action: (Player) -> Unit
    ): GuiItem<Player, ItemStack> =
        ItemBuilder.from(material).name(name).lore(lore.mm()).asGuiItem { player, _ -> action(player) }

    /**
     * Create the GUI for the rules.
     *
     * @return The GUI for the rules.
     */
    fun gui(): Gui = buildGui {
//        TODO: set container size automatically. maybe look even into pagination?
        spamPreventionDuration = ConfigData().guiAntiSpamDuration
        title(Utils.firewatchFormat("Admin Panel").mm())
        statelessComponent {
            it.setItem(
                0, // TODO: autoincrement based on the number of items.
                item(
//                    TODO: material should be based on value type. green: Boolean(True), red: Boolean(False), string: String, yellow: Int.
                    Material.WRITABLE_BOOK,
//                  TODO: name should be config key.
                    Utils.mangoFormat("").mm(),
//                    TODO: lore should be config value + info on how to change it.
                    listOf("")
                ) { it.performCommand("admin") }
            )
        }
    }
}