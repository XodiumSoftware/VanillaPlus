package org.xodium.illyriaplus.guis

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.data.KingdomData
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window
import kotlin.uuid.ExperimentalUuidApi

/** GUI for viewing and managing a specific kingdom. */
@OptIn(ExperimentalUuidApi::class)
@Suppress("UnstableApiUsage")
internal object KingdomGui {
    private const val RENAME_ITEM_TITLE = "Rename Kingdom"
    private const val MEMBERS_ITEM_TITLE = "View Members"

    /**
     * Returns an item that opens an anvil window for renaming the kingdom.
     * @param kingdom The kingdom to rename
     * @return An InvUI item that opens the rename anvil on click
     */
    private fun renameItem(kingdom: KingdomData) =
        Item
            .builder()
            .setItemProvider(
                ItemStack.of(Material.NAME_TAG).apply {
                    setData(DataComponentTypes.ITEM_NAME, MM.deserialize(RENAME_ITEM_TITLE))
                },
            ).addClickHandler { _, click ->
                // TODO: fix renaming
                val gui =
                    Gui
                        .builder()
                        .setStructure("# # #")
                        .build()

                AnvilWindow
                    .builder()
                    .setTextFieldAlwaysEnabled(true)
                    .setResultAlwaysValid(true)
                    .addRenameHandler {
                        gui.setItem(
                            2,
                            Item.simple(
                                ItemStack.of(Material.PAPER).apply {
                                    setData(DataComponentTypes.ITEM_NAME, MM.deserialize(it))
                                },
                            ),
                        )
                        kingdom.displayName(MM.deserialize(it))
                    }.setUpperGui(gui)
                    .open(click.player)
            }.build()

    /**
     * Returns an item that opens the members GUI when clicked.
     * @param kingdom The kingdom whose members to display
     * @return An InvUI item that opens the members GUI on click
     */
    private fun membersItem(kingdom: KingdomData): Item =
        Item
            .builder()
            .setItemProvider(
                ItemStack.of(Material.PLAYER_HEAD).apply {
                    setData(DataComponentTypes.ITEM_NAME, MM.deserialize(MEMBERS_ITEM_TITLE))
                },
            ).addClickHandler { _, click -> KingdomMembersGui.window(click.player, kingdom).open() }
            .build()

    /**
     * Builds and returns a GUI window for viewing a specific kingdom.
     * @param player The player viewing the GUI
     * @param kingdom The kingdom to display
     * @return The configured Window instance
     */
    fun window(
        player: Player,
        kingdom: KingdomData,
    ): Window =
        Window
            .builder()
            .setTitle(kingdom.displayName())
            .setUpperGui(
                Gui
                    .builder()
                    .setStructure("# # M R #")
                    .addIngredient('#', Utils.GUI.FILLER_ITEM)
                    .addIngredient('M', membersItem(kingdom))
                    .addIngredient('R', renameItem(kingdom))
                    .build(),
            ).setViewer(player)
            .build()
}
