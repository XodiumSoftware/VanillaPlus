/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.scheduler.BukkitTask
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Database
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.moveBowlsAndBottles
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * AutoRefillModule
 * Automatically refills the player's main hand and off hand slots when they consume an item
 */
class AutoRefillModule : ModuleInterface {
    /**
     * @return true if the module is enabled
     */
    override fun enabled(): Boolean = Config.AutoRefillModule.ENABLED

    /**
     * @return the command for the module
     */
    @Suppress("UnstableApiUsage")
    override fun cmd(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("autorefill")
            .requires { it.sender.hasPermission(Perms.AutoRefill.USE) }
            .executes(Command { Utils.tryCatch(it) { toggle(it.sender as Player) } })
    }

    private val cooldowns = ConcurrentHashMap<UUID, Long>()
    private val cooldownMs = 250L
    private val offHandSlot = 40

    private var cleanupTask: BukkitTask? = null

    // TODO: refill toggle doesnt immediately update the player's refill status, maybe todo with the cooldown?

    private val cooldowns = ConcurrentHashMap<UUID, Long>()
    private val cooldownMs = 250L
    private val offHandSlot = 40

    private var cleanupTask: BukkitTask? = null

    init {
        if (enabled()) {
            Database.createTable(this::class)
            startCleanupTask()
            Runtime.getRuntime().addShutdownHook(Thread { cleanup() })
        }
    }

    /**
     * Starts the cleanup task
     */
    private fun startCleanupTask() {
        cleanupTask = instance.server.scheduler.runTaskTimer(
            instance,
            Runnable { cooldowns.entries.removeIf { System.currentTimeMillis() - it.value > cooldownMs * 2 } },
            1200L,
            6000L
        )
        instance.logger.info("Started AutoRefill cooldown cleanup task")
    }

    /**
     * Cleans up the module resources
     */
    private fun cleanup() {
        synchronized(this) {
            cleanupTask?.cancel()
            cleanupTask = null
            cooldowns.clear()
            instance.logger.info("Cleaned up AutoRefill module resources")
        }
    }

    /**
     * Handles the PlayerItemConsumeEvent
     * @param event the event to handle
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerItemConsumeEvent) {
        if (!canAttemptRefill(event.player)) return
        attemptRefill(event.getPlayer())
    }

    /**
     * Refills the player's main hand and off hand slots when they place a block
     * @param event the BlockPlaceEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockPlaceEvent) {
        if (!canAttemptRefill(event.player)) return
        attemptRefill(event.getPlayer())
    }

    /**
     * Handles the PlayerInteractEvent to refill the player's main hand and off hand slots
     * @param event the PlayerInteractEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (!canAttemptRefill(event.player)) return
        attemptRefill(event.getPlayer())
    }

    /**
     * Checks if the player can attempt to refill
     * @param player the player to check
     * @return true if the player can attempt to refill
     */
    private fun canAttemptRefill(player: Player): Boolean {
        if (!player.hasPermission(Perms.AutoRefill.USE)) return false
        if (!isEnabledForPlayer(player)) return false

        val now = System.currentTimeMillis()
        val lastAttempt = cooldowns.getOrDefault(player.uniqueId, 0L)
        if (now - lastAttempt < cooldownMs) return false

        cooldowns[player.uniqueId] = now
        return true
    }

    /**
     * Attempts to refill the player's main hand and off hand slots
     * @param player the player to refill
     */
    private fun attemptRefill(player: Player) {
        val inventory = player.inventory

        val mainHandItem = inventory.itemInMainHand
        if (mainHandItem.type != Material.AIR && mainHandItem.amount == 1) {
            val mainHandSlot = inventory.heldItemSlot
            val refillSlot = findRefillSource(inventory, mainHandItem.type, mainHandSlot)
            if (refillSlot != -1) {
                refillStack(inventory, refillSlot, mainHandSlot, inventory.getItem(refillSlot))
            }
        }

        val offHandItem = inventory.itemInOffHand
        if (offHandItem.type != Material.AIR && offHandItem.amount == 1) {
            val refillSlot = findRefillSource(inventory, offHandItem.type, offHandSlot)
            if (refillSlot != -1) {
                refillStack(inventory, refillSlot, offHandSlot, inventory.getItem(refillSlot))
            }
        }
    }

    /**
     * Finds the best slot to refill the target slot
     * @param inventory the player's inventory
     * @param material the material to refill
     * @param currentSlot the current slot
     * @return the best slot to refill from
     */
    private fun findRefillSource(inventory: PlayerInventory, material: Material, currentSlot: Int): Int {
        var bestSlot = -1
        var smallestAmount = 65
        for (i in 0 until 36) {
            if (i == currentSlot) continue
            val item = inventory.getItem(i) ?: continue
            if (item.type != material) continue
            if (item.amount == 64) return i
        }

        for (i in 0 until 36) {
            if (i == currentSlot) continue
            val item = inventory.getItem(i) ?: continue
            if (item.type != material) continue
            if (item.amount < smallestAmount) {
                smallestAmount = item.amount
                bestSlot = i
            }
        }
        return bestSlot
    }

    /**
     * Refills the target slot with the source slot
     * @param inventory the player's inventory
     * @param source the source slot
     * @param target the target slot
     * @param itemStack the item to refill
     */
    private fun refillStack(inventory: Inventory, source: Int, target: Int, itemStack: ItemStack?) {
        if (itemStack == null) return
        instance.server.scheduler.runTask(instance, Runnable {
            try {
                val sourceItem = inventory.getItem(source)
                val targetItem = inventory.getItem(target)

                if (sourceItem == null || sourceItem.type != itemStack.type) return@Runnable
                if (targetItem != null && targetItem.type != Material.AIR &&
                    !moveBowlsAndBottles(inventory, target)
                ) {
                    return@Runnable
                }

                inventory.setItem(target, sourceItem.clone())
                inventory.setItem(source, null)
            } catch (e: Exception) {
                instance.logger.warning("Error during item refill: ${e.message}")
            }
        })
    }

    /**
     * Checks if AutoRefill is enabled for the given player
     * @param player the player to check
     * @return true if enabled (default), false if explicitly disabled
     */
    private fun isEnabledForPlayer(player: Player): Boolean =
        Database.getData(this::class, player.uniqueId.toString())?.lowercase() != "false"

    /**
     * Toggles AutoRefill for the given player
     * @param player the player to toggle
     */
    fun toggle(player: Player) {
        val currentValue = isEnabledForPlayer(player)
        val newValue = (!currentValue).toString()
        Database.setData(this::class, player.uniqueId.toString(), newValue)

        val status = if (!currentValue) "<green>ON" else "<red>OFF"
        player.sendActionBar(Utils.fireWatchFormat("AutoRefill: $status"))
    }
}