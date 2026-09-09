package org.xodium.illyriaplus.mechanics.entity

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.entity.WanderingTrader
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.data.WanderingTraderItemData
import org.xodium.illyriaplus.gui.WanderingTraderGui
import org.xodium.illyriaplus.mechanics.MechanicInterface
import java.io.File

/**
 * Replaces the wandering trader's trade GUI with a custom shop GUI shared by all wandering traders.
 * The stock is shared runtime state filled by players: any item sells for 1 emerald
 * ([SELL_PRICE_RATIO] of [DEFAULT_PRICE]) and resells for [DEFAULT_PRICE].
 */
internal object WanderingTraderMechanic : MechanicInterface {
    private const val PURCHASE_MSG = "<green>Purchase successful!"
    private const val NO_FUNDS_MSG = "<firewatch>You can't afford this item!</gradient>"
    private const val OUT_OF_STOCK_MSG = "<firewatch>The trader is out of stock!</gradient>"
    private const val SOLD_MSG = "<green>The trader accepted your items!"
    private const val EMERALDS_REJECTED_MSG = "<firewatch>The trader doesn't accept emeralds!</gradient>"
    private const val STOCK_FILE_NAME = "wandering_trader_stock.yml"

    /** Emerald price per item when buying; selling pays 50% of it (1 emerald). */
    private const val DEFAULT_PRICE = 2

    /** The fraction of [DEFAULT_PRICE] the trader pays when buying items from players. */
    private const val SELL_PRICE_RATIO = 0.5

    /** Emeralds paid per sold item: [SELL_PRICE_RATIO] of [DEFAULT_PRICE], at least 1. */
    private val SELL_PAYOUT = (DEFAULT_PRICE * SELL_PRICE_RATIO).toInt().coerceAtLeast(1)

    private val PURCHASE_SOUND: Sound =
        Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 1.0f, 1.0f)
    private val NO_FUNDS_SOUND: Sound =
        Sound.sound(Key.key("entity.villager.no"), Sound.Source.PLAYER, 1.0f, 1.0f)

    private val stockFile = File(instance.dataFolder, STOCK_FILE_NAME)

    /** Shared stock of all wandering traders, mapped to individual item counts per [Material]. */
    private val stock = mutableMapOf<Material, Int>()

    private var stockLoaded = false

    @EventHandler(ignoreCancelled = true)
    fun on(event: PlayerInteractEntityEvent) = handleInteract(event)

    /**
     * Opens the custom shop GUI when a player right-clicks any wandering trader,
     * suppressing the vanilla trade window.
     *
     * @param event The PlayerInteractEntityEvent triggered when a player interacts with an entity.
     */
    private fun handleInteract(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.rightClicked !is WanderingTrader) return
        event.isCancelled = true
        WanderingTraderGui.openShop(event.player, ::stockedTrades, ::stockOf, ::purchase, ::processDeposit)
    }

    /**
     * Returns the trade entries currently in stock, one per stocked material, alphabetically ordered.
     * Every entry is a single item priced at [DEFAULT_PRICE] emeralds.
     */
    private fun stockedTrades(): List<WanderingTraderItemData> {
        loadStock()
        return stock
            .filterValues { it > 0 }
            .keys
            .sortedBy { it.name }
            .map { WanderingTraderItemData(ItemStack.of(it), ItemStack.of(Material.EMERALD, DEFAULT_PRICE)) }
    }

    /**
     * Executes a purchase: buys up to [units] stacks of [item], capped by the available stock and
     * the player's emeralds, then deducts both and delivers the result. Overflow items are dropped
     * at the player's feet.
     *
     * @param player The purchasing player.
     * @param item The entry being purchased.
     * @param units The maximum number of stacks to buy (bulk purchase amount).
     */
    private fun purchase(
        player: Player,
        item: WanderingTraderItemData,
        units: Int,
    ) {
        loadStock()
        val inStock = (stock[item.result.type] ?: 0) / item.result.amount
        if (inStock == 0) {
            player.sendActionBar(MM.deserialize(OUT_OF_STOCK_MSG))
            player.playSound(NO_FUNDS_SOUND)
            return
        }

        val price = item.price
        val matching = player.inventory.all(price.type).filterValues { it.isSimilar(price) }
        val affordable = matching.values.sumOf { it.amount } / price.amount
        if (affordable == 0) {
            player.sendActionBar(MM.deserialize(NO_FUNDS_MSG))
            player.playSound(NO_FUNDS_SOUND)
            return
        }

        val bought = minOf(units, inStock, affordable)
        var remaining = price.amount * bought
        matching.forEach { (slot, stack) ->
            if (remaining <= 0) return@forEach
            val deduct = minOf(remaining, stack.amount)
            stack.amount -= deduct
            remaining -= deduct
            player.inventory.setItem(slot, stack.takeIf { it.amount > 0 })
        }

        stock[item.result.type] = (stock[item.result.type] ?: 0) - item.result.amount * bought
        saveStock()
        give(player, item.result.clone().apply { amount = item.result.amount * bought })
        player.sendActionBar(MM.deserialize(PURCHASE_MSG))
        player.playSound(PURCHASE_SOUND)
    }

    /**
     * Processes the contents of the sell window: every deposited item is added to the shared stock
     * and paid [SELL_PAYOUT] emerald(s) apiece. Emeralds are returned unprocessed.
     * Item meta is not preserved in the stock.
     *
     * @param player The selling player.
     * @param contents The deposited items, possibly containing null slots.
     */
    private fun processDeposit(
        player: Player,
        contents: List<ItemStack?>,
    ) {
        loadStock()
        var sold = false
        var rejected = false
        contents.filterNotNull().forEach { stack ->
            if (stack.type == Material.EMERALD) {
                give(player, stack)
                rejected = true
                return@forEach
            }
            stock.merge(stack.type, stack.amount, Int::plus)
            give(player, ItemStack.of(Material.EMERALD, SELL_PAYOUT * stack.amount))
            sold = true
        }
        if (sold) {
            saveStock()
            player.sendActionBar(MM.deserialize(SOLD_MSG))
            player.playSound(PURCHASE_SOUND)
        }
        if (rejected) {
            player.sendActionBar(MM.deserialize(EMERALDS_REJECTED_MSG))
            player.playSound(NO_FUNDS_SOUND)
        }
    }

    /**
     * Returns the current stock (individual items) of a trade entry.
     */
    private fun stockOf(item: WanderingTraderItemData): Int {
        loadStock()
        return stock[item.result.type] ?: 0
    }

    /**
     * Gives an item to the player, dropping overflow at their feet.
     */
    private fun give(
        player: Player,
        stack: ItemStack,
    ) {
        player
            .inventory
            .addItem(stack)
            .values
            .forEach { player.world.dropItemNaturally(player.location, it) }
    }

    /**
     * Lazily loads the stock from disk.
     */
    private fun loadStock() {
        if (stockLoaded) return
        stockLoaded = true
        if (!stockFile.exists()) return
        val config = YamlConfiguration.loadConfiguration(stockFile)
        config.getConfigurationSection("stock")?.getKeys(false)?.forEach { key ->
            Material.getMaterial(key)?.let { stock[it] = config.getInt("stock.$key") }
        }
    }

    /**
     * Persists the stock to disk. Entries with zero stock are omitted.
     */
    private fun saveStock() {
        val config = YamlConfiguration()
        stock.filterValues { it > 0 }.forEach { (type, amount) -> config.set("stock.${type.name}", amount) }
        runCatching {
            instance.dataFolder.mkdirs()
            config.save(stockFile)
        }.onFailure { instance.logger.warning("Failed to save wandering trader stock: ${it.message}") }
    }
}
