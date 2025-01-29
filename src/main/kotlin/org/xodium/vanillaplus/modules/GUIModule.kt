/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.incendo.interfaces.core.arguments.ArgumentKey
import org.incendo.interfaces.core.arguments.HashMapInterfaceArguments
import org.incendo.interfaces.core.click.ClickHandler
import org.incendo.interfaces.paper.PaperInterfaceListeners
import org.incendo.interfaces.paper.PlayerViewer
import org.incendo.interfaces.paper.element.ItemStackElement
import org.incendo.interfaces.paper.transform.PaperTransform
import org.incendo.interfaces.paper.type.ChestInterface
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class GUIModule : ModuleInterface {
    //    FIX: bug in the code, see console after opening GUI/clicking an item in it.
    override fun init() {
        PaperInterfaceListeners.install(instance)
    }

    fun openFAQ(player: Player) {
        faqInterface().open(
            PlayerViewer.of(player),
            HashMapInterfaceArguments
                .with(ArgumentKey.of("player", Player::class.java), player)
                .with(ArgumentKey.of("time", String::class.java)) {
                    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now())
                }
                .with<Int?>(ArgumentKey.of<Int?>("clicks", Int::class.java), 0).build()
        )
    }

    private fun faqInterface(): ChestInterface {
        return ChestInterface.builder()
            .title(Component.text("FAQ"))
            .rows(1)
            .updates(true, 5)
            .clickHandler(ClickHandler.cancel())
            .addTransform(PaperTransform.chestFill(ItemStackElement.of(ItemStack(Material.BLACK_STAINED_GLASS_PANE).apply {
                itemMeta = itemMeta?.apply {
                    displayName(
                        Component.text("")
                    )
                }
            })))
            .addTransform { pane, view ->
                val clicks: Int = view.arguments().get(ArgumentKey.of("clicks", Int::class.java))
                pane.element(
                    ItemStackElement.of(
                        ItemStack(Material.PAPER).apply {
                            itemMeta = itemMeta?.apply {
                                displayName(Component.text("Item Name", NamedTextColor.GREEN))
                                lore(
                                    arrayListOf(
                                        Component.text("Line 1"),
                                        Component.text("Clicks: $clicks")
                                    )
                                )
                            }
                        }
                    ) { clickHandler ->
                        clickHandler.view().backing().open(
                            clickHandler.viewer(), HashMapInterfaceArguments.Builder()
                                .with(
                                    ArgumentKey.of("clicks", Int::class.java),
                                    clickHandler.view().arguments().get(ArgumentKey.of("clicks", Int::class.java)) + 1
                                )
                                .with(
                                    ArgumentKey.of("player", Player::class.java),
                                    clickHandler.view().arguments().get(ArgumentKey.of("player", Player::class.java))
                                )
                                .build()
                        )
                    }, 4, 0
                )
            }
            .build()
    }

    override fun enabled(): Boolean = Config.GUIModule.ENABLED
}