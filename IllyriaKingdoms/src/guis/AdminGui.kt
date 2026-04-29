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
import org.xodium.illyriaplus.utils.Utils
import org.xodium.illyriaplus.utils.Utils.MM
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window
import kotlin.uuid.ExperimentalUuidApi

/** Admin GUI for managing kingdoms with paginated list view. */
@Suppress("UnstableApiUsage")
@OptIn(ExperimentalUuidApi::class)
internal object AdminGui {
    private const val GUI_TITLE = "<b><gradient:#FFA751:#FFE259>Kingdoms Admin Panel</gradient></b>"

    /**
     * Returns an item representing the given kingdom with lore and click handlers.
     * @param kingdom The kingdom data to display
     * @return An InvUI item with click handlers for viewing/deleting the kingdom
     */
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
                    KingdomGui.window(click.player, kingdom).open()
                }
            }.build()

    /** Item for creating a new kingdom (gives the player a Sceptre). */
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

    /**
     * Builds and returns the admin GUI window for the given player.
     * @param player The player viewing the GUI
     * @return The configured Window instance
     */
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
                        "# # # < C > # # #",
                    ).addIngredient('#', Utils.GUI.FILLER_ITEM)
                    .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                    .addIngredient('C', create)
                    .addIngredient('<', Utils.GUI.PREVIOUS_PAGE_ITEM)
                    .addIngredient('>', Utils.GUI.NEXT_PAGE_ITEM)
                    .setContent(KingdomManager.getAll().map { kingdomItem(it) })
                    .build(),
            ).setViewer(player)
            .build()
}
