package org.xodium.illyriaplus.mechanics.entity

import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.entity.WanderingTrader
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.persistence.PersistentDataType
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.Utils.Command.playerExecuted
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.data.CommandData
import org.xodium.illyriaplus.data.MerchantItemData
import org.xodium.illyriaplus.gui.MerchantGui
import org.xodium.illyriaplus.mechanics.MechanicInterface

/** Represents a mechanic handling the travelling merchant shop GUI on tagged wandering traders. */
internal object MerchantMechanic : MechanicInterface {
    private const val TRADER_NAME = "<mango><b>Travelling Merchant</b></gradient>"
    private const val SPAWNED_MSG = "<mango>Travelling Merchant spawned!</gradient>"
    private const val PURCHASE_MSG = "<green>Purchase successful!"
    private const val NO_FUNDS_MSG = "<firewatch>You can't afford this item!</gradient>"

    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("merchant")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .playerExecuted { player, _ -> player.spawnMerchant() },
                "Spawns a travelling merchant with a shop GUI",
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.merchant".lowercase(),
                "Allows use of the merchant command",
                PermissionDefault.OP,
            ),
        )

    private val MERCHANT_KEY = NamespacedKey(instance, "merchant")

    private val MERCHANT_ITEMS =
        listOf(
            MerchantItemData(ItemStack.of(Material.AMETHYST_SHARD, 4), ItemStack.of(Material.EMERALD)),
            MerchantItemData(ItemStack.of(Material.BAMBOO, 16), ItemStack.of(Material.EMERALD)),
            MerchantItemData(ItemStack.of(Material.BLUE_ICE, 8), ItemStack.of(Material.EMERALD)),
            MerchantItemData(ItemStack.of(Material.CHORUS_FRUIT, 4), ItemStack.of(Material.EMERALD)),
            MerchantItemData(ItemStack.of(Material.ECHO_SHARD), ItemStack.of(Material.EMERALD, 16)),
            MerchantItemData(ItemStack.of(Material.HEART_OF_THE_SEA), ItemStack.of(Material.EMERALD, 32)),
            MerchantItemData(ItemStack.of(Material.MOSS_BLOCK, 16), ItemStack.of(Material.EMERALD)),
            MerchantItemData(ItemStack.of(Material.NAUTILUS_SHELL, 2), ItemStack.of(Material.EMERALD, 8)),
            MerchantItemData(ItemStack.of(Material.PACKED_ICE, 16), ItemStack.of(Material.EMERALD)),
            MerchantItemData(ItemStack.of(Material.SPORE_BLOSSOM), ItemStack.of(Material.EMERALD, 4)),
        )

    private val PURCHASE_SOUND: Sound =
        Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.PLAYER, 1.0f, 1.0f)
    private val NO_FUNDS_SOUND: Sound =
        Sound.sound(Key.key("entity.villager.no"), Sound.Source.PLAYER, 1.0f, 1.0f)

    @EventHandler(ignoreCancelled = true)
    fun on(event: PlayerInteractEntityEvent) = handleInteract(event)

    /**
     * Opens the shop GUI when a player right-clicks a wandering trader tagged as a travelling merchant,
     * suppressing the vanilla trade window.
     *
     * @param event The PlayerInteractEntityEvent triggered when a player interacts with an entity.
     */
    private fun handleInteract(event: PlayerInteractEntityEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        val trader = event.rightClicked as? WanderingTrader ?: return
        if (!trader.persistentDataContainer.has(MERCHANT_KEY)) return
        event.isCancelled = true
        MerchantGui.open(event.player, MERCHANT_ITEMS, ::purchase)
    }

    /**
     * Spawns a wandering trader tagged as a travelling merchant at this player's location.
     */
    private fun Player.spawnMerchant() {
        world.spawn(location, WanderingTrader::class.java) {
            it.persistentDataContainer.set(MERCHANT_KEY, PersistentDataType.BOOLEAN, true)
            it.customName(MM.deserialize(TRADER_NAME))
            it.isCustomNameVisible = true
            it.isPersistent = true
        }
        sendActionBar(MM.deserialize(SPAWNED_MSG))
    }

    /**
     * Executes a purchase: verifies the player can pay the price, deducts it, and delivers the result.
     * Overflow items are dropped at the player's feet.
     *
     * @param player The purchasing player.
     * @param item The merchant entry being purchased.
     */
    private fun purchase(
        player: Player,
        item: MerchantItemData,
    ) {
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

        player.inventory.addItem(item.result.clone()).values.forEach {
            player.world.dropItemNaturally(player.location, it)
        }
        player.sendActionBar(MM.deserialize(PURCHASE_MSG))
        player.playSound(PURCHASE_SOUND)
    }
}
