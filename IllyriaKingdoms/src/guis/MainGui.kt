package org.xodium.illyriaplus.guis

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.interfaces.GuiInterface
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

/** A demo GUI showing a basic InvUI setup with a clickable dragon breath item. */
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
            .addClickHandler { TODO("trigger rename") }
            .build()

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
            .setTitle("Kingdom Menu")
            .setUpperGui(gui)
}
