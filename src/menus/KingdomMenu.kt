@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.menus

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.KingdomData
import org.xodium.vanillaplus.utils.Utils.MM
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.ItemWrapper
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.item.impl.controlitem.PageItem
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window
import java.util.*
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

        val membersBtn =
            object : AbstractItem() {
                override fun getItemProvider() =
                    ItemWrapper(
                        ItemStack.of(Material.PLAYER_HEAD).apply {
                            setData(
                                DataComponentTypes.CUSTOM_NAME,
                                MM.deserialize("<b>Members</b> <dark_gray>(${kingdom.members.size})"),
                            )
                        },
                    )

                override fun handleClick(
                    clickType: ClickType,
                    player: Player,
                    event: InventoryClickEvent,
                ) {
                    if (clickType.isLeftClick) members(player, kingdom)
                }
            }

        Window
            .single()
            .setTitle(kingdom.name)
            .setGui(
                Gui
                    .normal()
                    .setStructure("R . M . D")
                    .addIngredient('R', rename)
                    .addIngredient('M', membersBtn)
                    .addIngredient('D', disband)
                    .build(),
            ).open(player)
    }

    /** Opens the paginated members list GUI for [player] showing all members of [kingdom]. */
    fun members(
        player: Player,
        kingdom: KingdomData,
    ) {
        val items =
            kingdom.members.map { uuid ->
                val offline = instance.server.getOfflinePlayer(UUID.fromString(uuid.toString()))

                SimpleItem(
                    ItemWrapper(
                        ItemStack.of(Material.PLAYER_HEAD).apply {
                            setData(
                                DataComponentTypes.CUSTOM_NAME,
                                MM.deserialize("<white>${offline.name ?: uuid}"),
                            )
                            if (uuid == kingdom.owner) {
                                setData(
                                    DataComponentTypes.LORE,
                                    ItemLore.lore().addLines(listOf(MM.deserialize("<gold>Owner</gold>"))),
                                )
                            }
                        },
                    ),
                )
            }

        val back =
            object : PageItem(false) {
                override fun getItemProvider(gui: PagedGui<*>) =
                    ItemWrapper(
                        ItemStack.of(Material.ARROW).apply {
                            setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize("<gray>Previous Page"))
                        },
                    )
            }

        val forward =
            object : PageItem(true) {
                override fun getItemProvider(gui: PagedGui<*>) =
                    ItemWrapper(
                        ItemStack.of(Material.ARROW).apply {
                            setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize("<gray>Next Page"))
                        },
                    )
            }

        val border =
            SimpleItem(
                ItemWrapper(
                    ItemStack.of(Material.GRAY_STAINED_GLASS_PANE).apply {
                        setData(DataComponentTypes.CUSTOM_NAME, MM.deserialize("<!italic><dark_gray> "))
                    },
                ),
            )

        Window
            .single()
            .setTitle("${kingdom.name} — Members")
            .setGui(
                PagedGui
                    .items()
                    .setStructure(
                        "# # # # # # # # #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# # # < # > # # #",
                    ).addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                    .addIngredient('#', border)
                    .addIngredient('<', back)
                    .addIngredient('>', forward)
                    .setContent(items)
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
