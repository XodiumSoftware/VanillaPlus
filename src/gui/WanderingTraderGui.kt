package org.xodium.illyriaplus.gui

import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.data.WanderingTraderItemData
import xyz.xenondevs.commons.provider.mutableProvider
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

/** Builds and opens the wandering trader's custom shop GUI. */
@OptIn(ExperimentalDslApi::class)
internal object WanderingTraderGui {
    private const val TITLE = "<mango>Wandering Trader"
    private const val SELL_TITLE = "<mango>Sell to the Trader"
    private const val PREVIOUS_PAGE_NAME = "<gray>Previous page"
    private const val NEXT_PAGE_NAME = "<gray>Next page"
    private const val SELL_BUTTON_NAME = "<green><b>Sell items"
    private const val BULK_HINT = "<dark_gray>LMB: 1x | MMB: 10x | RMB: 100x"
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
     * Builds and opens the wandering trader shop window for the given player.
     *
     * @param player The player viewing the shop.
     * @param items Supplies the currently stocked trade entries, re-evaluated each time the window is built.
     * @param stockOf Returns the current stock (individual items) of an entry.
     * @param onPurchase Called when an entry is clicked, receiving the clicking player and the entry.
     * @param onDeposit Called with the contents of the sell window when it closes,
     * receiving the player and the deposited items.
     */
    fun openShop(
        player: Player,
        items: () -> List<WanderingTraderItemData>,
        stockOf: (WanderingTraderItemData) -> Int,
        onPurchase: (Player, WanderingTraderItemData, Int) -> Unit,
        onDeposit: (Player, List<ItemStack?>) -> Unit,
    ) {
        buildShopWindow(player, items, stockOf, onPurchase, onDeposit).open()
    }

    /**
     * Builds the shop window, including paged content and navigation.
     */
    private fun buildShopWindow(
        player: Player,
        items: () -> List<WanderingTraderItemData>,
        stockOf: (WanderingTraderItemData) -> Int,
        onPurchase: (Player, WanderingTraderItemData, Int) -> Unit,
        onDeposit: (Player, List<ItemStack?>) -> Unit,
    ): Window {
        lateinit var rebuild: () -> Unit
        val contentProvider = mutableProvider(emptyList<Item>())
        rebuild = { contentProvider.set(items().map { it.toGuiItem(stockOf, onPurchase, rebuild) }) }
        rebuild()

        return window(player) {
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
                                openSell(player, onDeposit) {
                                    buildShopWindow(player, items, stockOf, onPurchase, onDeposit)
                                }
                            }
                        }
                    '>' by forward
                    content by contentProvider
                }
        }
    }

    /**
     * Builds and opens the sell window: a deposit inventory whose contents are processed on close.
     * When the player closes the window themselves, [shopWindow] is rebuilt and reopened (deferred
     * one tick, as opening a window during close handling is not allowed, and rebuilt after the
     * deposit is processed so new stock shows up immediately).
     */
    private fun openSell(
        player: Player,
        onDeposit: (Player, List<ItemStack?>) -> Unit,
        shopWindow: () -> Window,
    ) {
        val deposit = VirtualInventory(DEPOSIT_SIZE)
        window(player) {
            title by MM.deserialize(SELL_TITLE)
            upperGui by deposit
            onClose {
                onDeposit(player, deposit.items.toList())
                if (reason == InventoryCloseEvent.Reason.PLAYER) {
                    player.scheduler.runDelayed(instance, { shopWindow().open() }, null, 1L)
                }
            }
        }.open()
    }

    /**
     * Builds the button displaying this trade entry, describing price and current stock in its lore.
     * [onContentChanged] runs on every click, and middle/right clicks buy 10x/100x.
     */
    private fun WanderingTraderItemData.toGuiItem(
        stockOf: (WanderingTraderItemData) -> Int,
        onPurchase: (Player, WanderingTraderItemData, Int) -> Unit,
        onContentChanged: () -> Unit,
    ): Item =
        item {
            itemProvider by provider { ItemBuilder(icon(stockOf)) }
            onClick {
                val units =
                    when (clickType) {
                        ClickType.MIDDLE -> 10
                        ClickType.RIGHT -> 100
                        else -> 1
                    }
                onPurchase(player, this@toGuiItem, units)
                onContentChanged()
            }
        }

    /**
     * Builds the display icon for this trade entry, with lore lines describing the price
     * and the current stock.
     */
    private fun WanderingTraderItemData.icon(stockOf: (WanderingTraderItemData) -> Int): ItemStack =
        result.clone().apply {
            editMeta { meta ->
                val stock = stockOf(this@icon)
                val lore = meta.lore()?.toMutableList() ?: mutableListOf()
                lore.add(
                    MM
                        .deserialize("<gray>Price: ${price.amount}x <sprite:items:item/${price.type.key.key}>")
                        .decoration(TextDecoration.ITALIC, false),
                )
                lore.add(
                    MM.deserialize("<gray>In stock: $stock").decoration(TextDecoration.ITALIC, false),
                )
                lore.add(MM.deserialize(BULK_HINT).decoration(TextDecoration.ITALIC, false))
                meta.lore(lore)
            }
        }
}
