package org.xodium.illyriaplus.gui

import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.data.MerchantItemData
import xyz.xenondevs.invui.dsl.ExperimentalDslApi
import xyz.xenondevs.invui.dsl.item
import xyz.xenondevs.invui.dsl.pagedItemsGui
import xyz.xenondevs.invui.dsl.window
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.item.ItemWrapper

/** Builds and opens the travelling merchant's shop GUI. */
@OptIn(ExperimentalDslApi::class)
internal object MerchantGui {
    private const val TITLE = "<mango>Travelling Merchant"
    private const val PREVIOUS_PAGE_NAME = "<gray>Previous page"
    private const val NEXT_PAGE_NAME = "<gray>Next page"

    private val BORDER = Item.simple(ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).hideTooltip(true))

    private val back =
        BoundItem
            .pagedBuilder()
            .setItemProvider { _, gui ->
                if (gui.page > 0) {
                    ItemBuilder(Material.ARROW).setName(PREVIOUS_PAGE_NAME)
                } else {
                    ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).hideTooltip(true)
                }
            }.addClickHandler { _, gui, _ -> gui.page-- }

    private val forward =
        BoundItem
            .pagedBuilder()
            .setItemProvider { _, gui ->
                if (gui.page < gui.pageCount - 1) {
                    ItemBuilder(Material.ARROW).setName(NEXT_PAGE_NAME)
                } else {
                    ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).hideTooltip(true)
                }
            }.addClickHandler { _, gui, _ -> gui.page++ }

    /**
     * Builds and opens the travelling merchant window for the given player.
     *
     * @param player The player viewing the shop.
     * @param items The merchant entries to display as paged content.
     * @param onPurchase Called when an entry is clicked, receiving the clicking player and the entry.
     */
    fun open(
        player: Player,
        items: List<MerchantItemData>,
        onPurchase: (Player, MerchantItemData) -> Unit,
    ) {
        window(player) {
            title by MM.deserialize(TITLE)
            upperGui by
                pagedItemsGui(
                    "# # # # # # # # #",
                    "# x x x x x x x #",
                    "# x x x x x x x #",
                    "# # # < # > # # #",
                ) {
                    '#' by BORDER
                    'x' by Markers.CONTENT_LIST_SLOT_HORIZONTAL
                    '<' by back
                    '>' by forward
                    content by items.map { it.toGuiItem(onPurchase) }
                }
        }.open()
    }

    /**
     * Builds the button displaying this merchant entry, describing the price in its lore.
     */
    private fun MerchantItemData.toGuiItem(onPurchase: (Player, MerchantItemData) -> Unit): Item =
        item {
            itemProvider by ItemWrapper(icon())
            onClick { onPurchase(player, this@toGuiItem) }
        }

    /**
     * Builds the display icon for this merchant entry, appending a lore line describing the price.
     */
    private fun MerchantItemData.icon(): ItemStack =
        result.clone().apply {
            editMeta { meta ->
                val lore = meta.lore()?.toMutableList() ?: mutableListOf()
                lore.add(
                    MM
                        .deserialize("<gray>Price: ${price.amount}x ")
                        .decoration(TextDecoration.ITALIC, false)
                        .append(price.displayName()),
                )
                meta.lore(lore)
            }
        }
}
