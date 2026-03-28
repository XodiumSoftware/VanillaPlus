package org.xodium.vanillaplus.menus

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.data.KingdomData
import org.xodium.vanillaplus.utils.Utils.MM
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window
import kotlin.uuid.ExperimentalUuidApi

/** GUI for the kingdom management screen. */
@Suppress("UnstableApiUsage")
@OptIn(ExperimentalUuidApi::class)
internal object KingdomMenu {
    /** Opens the kingdom management GUI for [player] showing their [kingdom]. */
    fun main(
        player: Player,
        kingdom: KingdomData,
    ) {
        val disband =
            object : AbstractItem() {
                override fun getItemProvider() =
                    ItemWrapper(
                        ItemStack.of(Material.BARRIER).apply {
                            setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize("<b><red>Disband Kingdom</red></b>"))
                        },
                    )

                override fun handleClick(
                    clickType: ClickType,
                    player: Player,
                    event: InventoryClickEvent,
                ) {
                    if (clickType.isLeftClick) {
                        kingdom.delete()
                        player.closeInventory()
                    }
                }
            }

        val rename =
            object : AbstractItem() {
                override fun getItemProvider() =
                    ItemWrapper(
                        ItemStack.of(Material.FEATHER).apply {
                            setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize("<b>Rename Kingdom</b>"))
                        },
                    )

                override fun handleClick(
                    clickType: ClickType,
                    player: Player,
                    event: InventoryClickEvent,
                ) {
                    if (clickType.isLeftClick) rename(player, kingdom)
                }
            }

        Window
            .single()
            .setTitle(kingdom.name)
            .setGui(
                Gui
                    .normal()
                    .setStructure("R . . . D")
                    .addIngredient('R', rename)
                    .addIngredient('D', disband)
                    .build(),
            ).open(player)
    }

    /** Opens the kingdom rename anvil GUI for [player], saving the new name on confirm. */
    fun rename(
        player: Player,
        kingdom: KingdomData,
    ) {
        var newName = kingdom.name

        val confirm =
            object : AbstractItem() {
                override fun getItemProvider() =
                    ItemWrapper(
                        ItemStack.of(Material.LIME_DYE).apply {
                            setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize("<b><green>Confirm</green></b>"))
                        },
                    )

                override fun handleClick(
                    clickType: ClickType,
                    player: Player,
                    event: InventoryClickEvent,
                ) {
                    if (clickType.isLeftClick) {
                        kingdom.copy(name = newName).save()
                        player.closeInventory()
                    }
                }
            }

        AnvilWindow
            .single()
            .setTitle("Rename Kingdom")
            .setGui(
                Gui
                    .normal()
                    .setStructure("I . C")
                    .addIngredient(
                        'I',
                        SimpleItem(
                            ItemWrapper(
                                ItemStack.of(Material.PAPER).apply {
                                    setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize(kingdom.name))
                                },
                            ),
                        ),
                    ).addIngredient('C', confirm)
                    .build(),
            ).addRenameHandler { newName = it }
            .open(player)
    }
}
