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
 * The stock is shared runtime state filled by players: any item can be sold; items without a
 * curated price are bought for [DEFAULT_PRICE] emeralds and sold at [SELL_PRICE_RATIO] of it.
 */
internal object WanderingTraderMechanic : MechanicInterface {
    private const val PURCHASE_MSG = "<green>Purchase successful!"
    private const val NO_FUNDS_MSG = "<firewatch>You can't afford this item!</gradient>"
    private const val OUT_OF_STOCK_MSG = "<firewatch>The trader is out of stock!</gradient>"
    private const val SOLD_MSG = "<green>The trader accepted your items!"
    private const val STOCK_FILE_NAME = "wandering_trader_stock.yml"

    /** Emerald price per item when buying something without a curated price. Selling pays 50% of it (1 emerald). */
    private const val DEFAULT_PRICE = 2

    /** The fraction of the listed price the trader pays when buying items from players. */
    private const val SELL_PRICE_RATIO = 0.5

    /** Curated trades: buy price per stack for notable items. Uncurated items fall back to [DEFAULT_PRICE]. */
    private val TRADES =
        listOf(
            WanderingTraderItemData(ItemStack.of(Material.AMETHYST_SHARD, 4), ItemStack.of(Material.EMERALD)),
            WanderingTraderItemData(ItemStack.of(Material.BAMBOO, 16), ItemStack.of(Material.EMERALD)),
            WanderingTraderItemData(ItemStack.of(Material.BLUE_ICE, 8), ItemStack.of(Material.EMERALD)),
            WanderingTraderItemData(ItemStack.of(Material.CHORUS_FRUIT, 4), ItemStack.of(Material.EMERALD)),
            WanderingTraderItemData(ItemStack.of(Material.ECHO_SHARD), ItemStack.of(Material.EMERALD, 16)),
            WanderingTraderItemData(ItemStack.of(Material.HEART_OF_THE_SEA), ItemStack.of(Material.EMERALD, 32)),
            WanderingTraderItemData(ItemStack.of(Material.MOSS_BLOCK, 16), ItemStack.of(Material.EMERALD)),
            WanderingTraderItemData(ItemStack.of(Material.NAUTILUS_SHELL, 2), ItemStack.of(Material.EMERALD, 8)),
            WanderingTraderItemData(ItemStack.of(Material.PACKED_ICE, 16), ItemStack.of(Material.EMERALD)),
            WanderingTraderItemData(ItemStack.of(Material.SPORE_BLOSSOM), ItemStack.of(Material.EMERALD, 4)),
        )

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
        WanderingTraderGui.openShop(event.player, stockedTrades(), ::stockOf, ::purchase, ::processDeposit)
    }

    /**
     * Returns the trade entries currently in stock, one per stocked material, alphabetically ordered.
     * Materials without a curated trade get single-item stacks priced at [DEFAULT_PRICE] emeralds.
     */
    private fun stockedTrades(): List<WanderingTraderItemData> {
        loadStock()
        return stock
            .filterValues { it > 0 }
            .keys
            .sortedBy { it.name }
            .map { type ->
                TRADES.firstOrNull { it.result.type == type }
                    ?: WanderingTraderItemData(ItemStack.of(type), ItemStack.of(Material.EMERALD, DEFAULT_PRICE))
            }
    }

    /**
     * Executes a purchase: verifies stock and that the player can pay the price, deducts both,
     * and delivers the result. Overflow items are dropped at the player's feet.
     *
     * @param player The purchasing player.
     * @param item The entry being purchased.
     */
    private fun purchase(
        player: Player,
        item: WanderingTraderItemData,
    ) {
        loadStock()
        if ((stock[item.result.type] ?: 0) < item.result.amount) {
            player.sendActionBar(MM.deserialize(OUT_OF_STOCK_MSG))
            player.playSound(NO_FUNDS_SOUND)
            return
        }

        val price = item.price
        val matching = player.inventory.all(price.type).filterValues { it.isSimilar(price) }
        if (matching.values.sumOf { it.amount } < price.amount) {
            player.sendActionBar(MM.deserialize(NO_FUNDS_MSG))
            player.playSound(NO_FUNDS_SOUND)
            return
        }

        var remaining = price.amount
        matching.forEach { (slot, stack) ->
            if (remaining <= 0) return@forEach
            val deduct = minOf(remaining, stack.amount)
            stack.amount -= deduct
            remaining -= deduct
            player.inventory.setItem(slot, stack.takeIf { it.amount > 0 })
        }

        stock[item.result.type] = (stock[item.result.type] ?: 0) - item.result.amount
        saveStock()
        give(player, item.result.clone())
        player.sendActionBar(MM.deserialize(PURCHASE_MSG))
        player.playSound(PURCHASE_SOUND)
    }

    /**
     * Processes the contents of the sell window: items are added to the shared stock and paid for
     * per full trade stack ([SELL_PRICE_RATIO] of the curated price, or 1 emerald per item without
     * one). Leftover partial stacks are returned; meta of uncurated items is not preserved in stock.
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
        contents.filterNotNull().forEach { stack ->
            val trade = TRADES.firstOrNull { it.result.isSimilar(stack) }
            if (trade == null) {
                stock.merge(stack.type, stack.amount, Int::plus)
                give(player, ItemStack.of(Material.EMERALD, stack.amount))
                sold = true
                return@forEach
            }

            val units = stack.amount / trade.result.amount
            if (units == 0) {
                give(player, stack)
                return@forEach
            }

            stock.merge(trade.result.type, units * trade.result.amount, Int::plus)
            val payout = (trade.price.amount * SELL_PRICE_RATIO).toInt().coerceAtLeast(1)
            give(player, ItemStack.of(trade.price.type, payout * units))
            sold = true

            val leftover = stack.amount % trade.result.amount
            if (leftover > 0) give(player, stack.asQuantity(leftover))
        }
        if (sold) {
            saveStock()
            player.sendActionBar(MM.deserialize(SOLD_MSG))
            player.playSound(PURCHASE_SOUND)
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
