package org.xodium.illyriaplus.guis

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.GuiInterface
import org.xodium.illyriaplus.pdcs.PlayerPDC.kingdom
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window
import kotlin.uuid.ExperimentalUuidApi

/** A demo GUI showing a basic InvUI setup with a clickable dragon breath item. */
@OptIn(ExperimentalUuidApi::class)
internal object MainGui : GuiInterface {
    /** The clickable item displayed in the GUI that prints "TEST" when clicked. */
    private val FILLER_ITEM =
        Item
            .builder()
            .setItemProvider(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE))
            .build()

    /** The item that triggers rename functionality when clicked in the GUI. */
    private val RENAME_ITEM =
        Item
            .builder()
            .setItemProvider(ItemStack.of(Material.NAME_TAG))
            .addClickHandler { _, click ->
                AnvilWindow
                    .builder()
                    .setTitle(MM.deserialize("Enter New Kingdom Name"))
                    .setTextFieldAlwaysEnabled(true)
                    .addRenameHandler { click.player.kingdom?.displayName(MM.deserialize(it)) }
                    .open(click.player)
            }.build()

    override val gui =
        Gui
            .builder()
            .setStructure("# # # # # # R # #")
            .addIngredient('#', FILLER_ITEM)
            .addIngredient('R', RENAME_ITEM)
            .build()

    override val window =
        Window
            .builder()
            .setTitle("")
            .setUpperGui(gui)
}
