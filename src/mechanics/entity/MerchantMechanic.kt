package org.xodium.illyriaplus.mechanics.entity

import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.entity.WanderingTrader
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
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
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random

/** Represents a mechanic handling travelling merchants: tagged wandering traders that open a custom shop GUI. */
internal object MerchantMechanic : MechanicInterface {
    private const val TRADER_NAME = "<mango><b>Travelling Merchant</b></gradient>"
    private const val SPAWNED_MSG = "<mango>Travelling Merchant spawned!</gradient>"
    private const val PURCHASE_MSG = "<green>Purchase successful!"
    private const val NO_FUNDS_MSG = "<firewatch>You can't afford this item!</gradient>"

    /** Vanilla wandering trader lifetime (40 minutes), applied to merchants so they despawn naturally. */
    private const val DESPAWN_DELAY = 48000

    private const val SPAWN_DISTANCE_MIN = 10
    private const val SPAWN_DISTANCE_MAX = 25

    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("merchant")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .playerExecuted { player, _ -> player.spawnMerchant() },
                "Spawns a travelling merchant nearby, as if a wandering trader spawned early",
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

    @EventHandler(ignoreCancelled = true)
    fun on(event: CreatureSpawnEvent) = handleNaturalSpawn(event)

    /**
     * Opens the shop GUI when a player right-clicks a travelling merchant,
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
     * Converts every naturally spawned wandering trader into a travelling merchant.
     *
     * @param event The CreatureSpawnEvent triggered when a creature spawns.
     */
    private fun handleNaturalSpawn(event: CreatureSpawnEvent) {
        if (event.spawnReason != CreatureSpawnEvent.SpawnReason.NATURAL) return
        val trader = event.entity as? WanderingTrader ?: return
        makeMerchant(trader)
    }

    /**
     * Spawns a travelling merchant on the surface near this player, mimicking a natural
     * wandering trader spawn (natural despawn, non-persistent).
     */
    private fun Player.spawnMerchant() {
        val angle = Random.nextDouble(Math.PI * 2)
        val distance = Random.nextInt(SPAWN_DISTANCE_MIN, SPAWN_DISTANCE_MAX + 1)
        val x = floor(location.x + cos(angle) * distance)
        val z = floor(location.z + sin(angle) * distance)
        val y = world.getHighestBlockYAt(x.toInt(), z.toInt()) + 1.0
        world.spawn(
            Location(world, x, y, z),
            WanderingTrader::class.java,
            CreatureSpawnEvent.SpawnReason.CUSTOM,
            ::makeMerchant,
        )
        sendActionBar(MM.deserialize(SPAWNED_MSG))
    }

    /**
     * Tags a wandering trader as a travelling merchant and applies its name;
     * without persistence, so it despawns like a vanilla wandering trader.
     *
     * @param trader The wandering trader to convert.
     */
    private fun makeMerchant(trader: WanderingTrader) {
        trader.persistentDataContainer.set(MERCHANT_KEY, PersistentDataType.BOOLEAN, true)
        trader.customName(MM.deserialize(TRADER_NAME))
        trader.isCustomNameVisible = true
        trader.despawnDelay = DESPAWN_DELAY
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
