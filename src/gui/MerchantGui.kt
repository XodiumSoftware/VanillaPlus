package org.xodium.illyriaplus.gui

import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.data.MerchantItemData
import xyz.xenondevs.commons.provider.provider
import xyz.xenondevs.invui.dsl.ExperimentalDslApi
import xyz.xenondevs.invui.dsl.item
import xyz.xenondevs.invui.dsl.pagedItemsGui
import xyz.xenondevs.invui.dsl.window
import xyz.xenondevs.invui.gui.Markers
import xyz.xenondevs.invui.inventory.VirtualInventory
import xyz.xenondevs.invui.item.BoundItem
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.ItemBuilder
import xyz.xenondevs.invui.window.Window

/** Builds and opens the travelling merchant's shop GUI. */
@OptIn(ExperimentalDslApi::class)
internal object MerchantGui {
    private const val TITLE = "<mango>Travelling Merchant"
    private const val SELL_TITLE = "<mango>Sell to the Merchant"
    private const val PREVIOUS_PAGE_NAME = "<gray>Previous page"
    private const val NEXT_PAGE_NAME = "<gray>Next page"
    private const val SELL_BUTTON_NAME = "<green><b>Sell items"
    private const val DEPOSIT_SIZE = 27

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
     * Builds and opens the travelling merchant shop window for the given player.
     *
     * @param player The player viewing the shop.
     * @param items The merchant entries to display as paged content.
     * @param stockOf Returns the current stock (individual items) of an entry.
     * @param onPurchase Called when an entry is clicked, receiving the clicking player and the entry.
     * @param onDeposit Called with the contents of the sell window when it closes,
     * receiving the player and the deposited items.
     */
    fun openShop(
        player: Player,
        items: List<MerchantItemData>,
        stockOf: (MerchantItemData) -> Int,
        onPurchase: (Player, MerchantItemData) -> Unit,
        onDeposit: (Player, List<ItemStack?>) -> Unit,
    ) {
        buildShopWindow(player, items, stockOf, onPurchase, onDeposit).open()
    }

    /**
     * Builds the shop window, including paged content and navigation.
     */
    private fun buildShopWindow(
        player: Player,
        items: List<MerchantItemData>,
        stockOf: (MerchantItemData) -> Int,
        onPurchase: (Player, MerchantItemData) -> Unit,
        onDeposit: (Player, List<ItemStack?>) -> Unit,
    ): Window =
        window(player) {
            title by MM.deserialize(TITLE)
            upperGui by
                pagedItemsGui(
                    "# # # # # # # # #",
                    "# x x x x x x x #",
                    "# x x x x x x x #",
                    "# # # < s > # # #",
                ) {
                    '#' by BORDER
                    'x' by Markers.CONTENT_LIST_SLOT_HORIZONTAL
                    '<' by back
                    's' by
                        item {
                            itemProvider by ItemBuilder(Material.EMERALD).setName(SELL_BUTTON_NAME)
                            onClick {
                                val shopWindow = buildShopWindow(player, items, stockOf, onPurchase, onDeposit)
                                openSell(player, onDeposit, shopWindow)
                            }
                        }
                    '>' by forward
                    content by items.map { it.toGuiItem(stockOf, onPurchase) }
                }
        }

    /**
     * Builds and opens the sell window: a deposit inventory whose contents are processed on close.
     * Closing the window returns the player to [shopWindow].
     */
    private fun openSell(
        player: Player,
        onDeposit: (Player, List<ItemStack?>) -> Unit,
        shopWindow: Window,
    ) {
        val deposit = VirtualInventory(DEPOSIT_SIZE)
        window(player) {
            title by MM.deserialize(SELL_TITLE)
            upperGui by deposit
            fallbackWindow by shopWindow
            onClose { onDeposit(player, deposit.items.toList()) }
        }.open()
    }

    /**
     * Builds the button displaying this merchant entry, describing price and current stock in its lore.
     * The stock line re-resolves whenever the item updates.
     */
    private fun MerchantItemData.toGuiItem(
        stockOf: (MerchantItemData) -> Int,
        onPurchase: (Player, MerchantItemData) -> Unit,
    ): Item {
        lateinit var self: Item
        self =
            item {
                itemProvider by provider { ItemBuilder(icon(stockOf)) }
                onClick {
                    onPurchase(player, this@toGuiItem)
                    self.notifyWindows()
                }
            }
        return self
    }

    /**
     * Builds the display icon for this merchant entry, appending lore lines describing the price
     * and the currently available stock (red when sold out).
     */
    private fun MerchantItemData.icon(stockOf: (MerchantItemData) -> Int): ItemStack =
        result.clone().apply {
            editMeta { meta ->
                val stock = stockOf(this@icon)
                val lore = meta.lore()?.toMutableList() ?: mutableListOf()
                lore.add(
                    MM
                        .deserialize("<gray>Price: ${price.amount}x ")
                        .decoration(TextDecoration.ITALIC, false)
                        .append(price.displayName()),
                )
                lore.add(
                    MM
                        .deserialize("${if (stock > 0) "<gray>" else "<red>"}In stock: $stock")
                        .decoration(TextDecoration.ITALIC, false),
                )
                meta.lore(lore)
            }
        }
}
