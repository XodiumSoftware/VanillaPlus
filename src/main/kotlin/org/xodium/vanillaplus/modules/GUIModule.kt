/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.container.GuiContainer
import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import dev.triumphteam.gui.paper.kotlin.builder.chestContainer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.interfaces.ModuleInterface

class GUIModule : ModuleInterface {
    fun faqGUI(): Gui {
        return buildGui {
            containerType = chestContainer { rows = 1 }
            title(Utils.MM.deserialize("<b><gradient:#CB2D3E:#EF473A>FAQ</gradient></b>"))
            statelessComponent {
                TODO("flip those 2 items")
                it.setItem(
                    1, 5, ItemBuilder.from(Material.DIAMOND)
                        .name(Utils.MM.deserialize("Click me!"))
                        .asGuiItem { player, _ ->
                            player.sendMessage(Utils.MM.deserialize("You have clicked on the diamond item!"))
                            dimGUI().open(player)
                        })
                filler(it)
            }
        }
    }

    fun dimGUI(): Gui {
        return buildGui {
            containerType = chestContainer { rows = 1 }
            title(Utils.MM.deserialize("<b><gradient:#CB2D3E:#EF473A>Dimensions</gradient></b>"))
            statelessComponent {
                it.setItem(
                    1, 5, ItemBuilder.from(Material.PAPER)
                        .name(Utils.MM.deserialize("Click me!"))
                        .asGuiItem { player, _ ->
                            player.sendMessage(Utils.MM.deserialize("You have clicked on the paper item!"))
                            player.performCommand("help")
                            settingsGUI().open(player)
                        })
            }
        }
    }

    fun settingsGUI(): Gui {
        return buildGui {
            containerType = chestContainer { rows = 1 }
            title(Utils.MM.deserialize("<b><gradient:#CB2D3E:#EF473A>Settings</gradient></b>"))
            statelessComponent {
                it.setItem(
                    1, 5, ItemBuilder.from(Material.SPRUCE_SAPLING)
                        .name(Utils.MM.deserialize("Click me!"))
                        .asGuiItem { player, _ ->
                            player.sendMessage(Utils.MM.deserialize("You have clicked on the paper item!"))
                            player.performCommand("help")
                        })
            }
        }
    }

    private fun filler(gui: GuiContainer<Player, ItemStack>) {
        for (slot in 0 until 9) {
            gui.setItem(
                slot, ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE)
                    .name(Utils.MM.deserialize(""))
                    .asGuiItem()
            )
        }
    }

    override fun enabled(): Boolean = Config.GUIModule.ENABLED
}