package org.xodium.illyriaplus.guis

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.data.KingdomData
import org.xodium.illyriaplus.utils.Utils.MM
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window
import kotlin.uuid.ExperimentalUuidApi

/** A demo GUI showing a basic InvUI setup with a clickable dragon breath item. */
@Suppress("UnstableApiUsage")
@OptIn(ExperimentalUuidApi::class)
internal object MainGui {
    /** The clickable item displayed in the GUI that prints "TEST" when clicked. */
    private val FILLER_ITEM =
        Item
            .builder()
            .setItemProvider(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE))
            .build()

    /** The item that triggers rename functionality when clicked in the GUI. */
    private fun renameItem(kingdom: KingdomData) =
        Item
            .builder()
            .setItemProvider(
                ItemStack.of(Material.NAME_TAG).apply {
                    setData(DataComponentTypes.ITEM_NAME, MM.deserialize("Rename Kingdom"))
                },
            ).addClickHandler { _, click ->
                AnvilWindow
                    .builder()
                    .setTitle(MM.deserialize("Enter New Kingdom Name"))
                    .setTextFieldAlwaysEnabled(true)
                    .addRenameHandler { kingdom.displayName(MM.deserialize(it)) }
                    .open(click.player)
            }.build()

    /** Creates a window for the given player with their kingdom name as the title. */
    fun window(
        player: Player,
        kingdom: KingdomData,
    ): Window {
        val gui =
            Gui
                .builder()
                .setStructure("# # # # # # R # #")
                .addIngredient('#', FILLER_ITEM)
                .addIngredient('R', renameItem(kingdom))
                .build()

        return Window
            .builder()
            .setTitle(kingdom.displayName())
            .setUpperGui(gui)
            .setViewer(player)
            .build()
    }
}
