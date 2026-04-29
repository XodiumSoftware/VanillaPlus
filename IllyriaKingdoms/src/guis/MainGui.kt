package org.xodium.illyriaplus.guis

import org.bukkit.entity.Player
import org.xodium.illyriaplus.data.KingdomData
import org.xodium.illyriaplus.utils.Utils
import org.xodium.illyriaplus.utils.Utils.MM
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window
import kotlin.uuid.ExperimentalUuidApi

/** A demo GUI showing a basic InvUI setup with a clickable dragon breath item. */
@OptIn(ExperimentalUuidApi::class)
internal object MainGui {
    private const val RENAME_ITEM_TITLE = "Rename Kingdom"
    private const val RENAME_INPUT_TITLE = "Enter New Kingdom Name"

    /** The item that triggers rename functionality when clicked in the GUI. */
    private fun renameItem(kingdom: KingdomData) =
        Item
            .builder()
            .setItemProvider(Utils.guiRenameItemStack(RENAME_ITEM_TITLE))
            .addClickHandler { _, click ->
                AnvilWindow
                    .builder()
                    .setTitle(MM.deserialize(RENAME_INPUT_TITLE))
                    .setTextFieldAlwaysEnabled(true)
                    .setResultAlwaysValid(true)
                    .addRenameHandler { kingdom.displayName(MM.deserialize(it)) }
                    .open(click.player)
            }.build()

    /** Creates a window for the given player with their kingdom name as the title. */
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
                    .setStructure("# # # # # # R # #")
                    .addIngredient('#', Utils.GUI_FILLER_ITEM)
                    .addIngredient('R', renameItem(kingdom))
                    .build(),
            ).setViewer(player)
            .build()
}
