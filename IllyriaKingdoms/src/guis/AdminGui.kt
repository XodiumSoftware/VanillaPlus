package org.xodium.illyriaplus.guis

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.data.KingdomData
import org.xodium.illyriaplus.items.SceptreItem
import org.xodium.illyriaplus.managers.KingdomManager
import org.xodium.illyriaplus.utils.Utils.GUI_FILLER_ITEM
import org.xodium.illyriaplus.utils.Utils.MM
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import kotlin.uuid.ExperimentalUuidApi

/** Admin GUI for managing kingdoms with paginated list view. */
@Suppress("UnstableApiUsage")
@OptIn(ExperimentalUuidApi::class)
internal object AdminGui {
    private const val GUI_TITLE = "<gradient:#FFA751:#FFE259>Kingdoms Admin Panel</gradient>"

    private fun kingdomItem(kingdom: KingdomData): Item =
        Item
            .builder()
            .setItemProvider(
                ItemStack.of(Material.PLAYER_HEAD).apply {
                    setData(DataComponentTypes.ITEM_NAME, kingdom.displayName())
                    setData(
                        DataComponentTypes.LORE,
                        ItemLore.lore(
                            listOf(
                                Component.empty(),
                                MM.deserialize("<gray>ID: <white>${kingdom.id}"),
                                MM.deserialize("<gray>Members: <white>${kingdom.members.size}"),
                                Component.empty(),
                                MM.deserialize("<yellow>Click to view details"),
                                MM.deserialize("<red>Shift+Click to delete"),
                            ),
                        ),
                    )
                },
            ).addClickHandler { _, click ->
                if (click.clickType.isShiftClick) {
                    KingdomManager.remove(kingdom.id)
                    click.player.sendMessage(MM.deserialize("<green>Kingdom '${kingdom.name}' has been removed."))
                    window(click.player).open()
                } else {
                    MainGui.window(click.player, kingdom).open()
                }
            }.build()

    private val create: Item =
        Item
            .builder()
            .setItemProvider(
                ItemStack.of(Material.EMERALD_BLOCK).apply {
                    setData(DataComponentTypes.ITEM_NAME, MM.deserialize("<green>Create Kingdom"))
                },
            ).addClickHandler { _, click ->
                click.player.inventory.addItem(SceptreItem.item)
                click.player.sendMessage(MM.deserialize("<green>Received a Kingdom Sceptre."))
            }.build()

    private val delete: Item =
        Item
            .builder()
            .setItemProvider(
                ItemStack.of(Material.REDSTONE_BLOCK).apply {
                    setData(DataComponentTypes.ITEM_NAME, MM.deserialize("<red>Delete Kingdom</red>"))
                    setData(
                        DataComponentTypes.LORE,
                        ItemLore.lore(listOf(MM.deserialize("<gray>Shift+Click a kingdom to delete it"))),
                    )
                },
            ).build()

    private val back: BoundItem =
        BoundItem
            .pagedBuilder()
            .setItemProvider(
                ItemStack.of(Material.ARROW).apply {
                    setData(DataComponentTypes.ITEM_NAME, MM.deserialize("<gray>Previous Page"))
                },
            ).addClickHandler { _, gui, _ -> gui.page++ }
            .build()

    private val forward: BoundItem =
        BoundItem
            .pagedBuilder()
            .setItemProvider(
                ItemStack.of(Material.ARROW).apply {
                    setData(DataComponentTypes.ITEM_NAME, MM.deserialize("<gray>Next Page"))
                },
            ).addClickHandler { _, gui, _ -> gui.page++ }
            .build()

    fun window(player: Player): Window =
        Window
            .builder()
            .setTitle(MM.deserialize(GUI_TITLE))
            .setUpperGui(
                PagedGui
                    .itemsBuilder()
                    .setStructure(
                        "# # # # # # # # #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# C # B # F # D #",
                    ).addIngredient('#', GUI_FILLER_ITEM)
                    .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                    .addIngredient('C', create)
                    .addIngredient('D', delete)
                    .addIngredient('F', forward)
                    .addIngredient('B', back)
                    .setContent(KingdomManager.getAll().map { kingdomItem(it) })
                    .build(),
            ).setViewer(player)
            .build()
}
