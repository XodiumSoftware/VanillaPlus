package org.xodium.illyriaplus.guis

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import org.xodium.illyriaplus.utils.Utils
import org.xodium.illyriaplus.utils.Utils.MM
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.window.Window

/** GUI for selecting an online player. */
@Suppress("UnstableApiUsage")
internal object PlayerSelectGui {
    private const val GUI_TITLE = "<b><gradient:#FFA751:#FFE259>Select Player</gradient></b>"

    /**
     * Returns an item representing the given player.
     * @param player The player to display.
     * @param onSelect Callback when this player is selected.
     * @return An InvUI item with click handler for selecting the player.
     */
    private fun playerItem(
        player: Player,
        onSelect: (Player) -> Unit,
    ): Item =
        Item
            .builder()
            .setItemProvider(
                ItemStack.of(Material.PLAYER_HEAD).apply {
                    setData(DataComponentTypes.ITEM_NAME, MM.deserialize("<green>${player.name}"))
                },
            ).addClickHandler { onSelect(player) }
            .build()

    /**
     * Builds and returns the player selection GUI window.
     * @param viewer The player viewing the GUI.
     * @param onSelect Callback when a player is selected.
     * @return The configured Window instance.
     */
    fun window(
        viewer: Player,
        onSelect: (Player) -> Unit,
    ): Window =
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
                        "# # # < - > # # #",
                    ).addIngredient('#', Utils.GUI.FILLER_ITEM)
                    .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                    .addIngredient('-', Utils.GUI.FILLER_ITEM)
                    .addIngredient('<', Utils.GUI.PREVIOUS_PAGE_ITEM)
                    .addIngredient('>', Utils.GUI.NEXT_PAGE_ITEM)
                    .setContent(instance.server.onlinePlayers.map { playerItem(it, onSelect) })
                    .build(),
            ).setViewer(viewer)
            .build()
}
