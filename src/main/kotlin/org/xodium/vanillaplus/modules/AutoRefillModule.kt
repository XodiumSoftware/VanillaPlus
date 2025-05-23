/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
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
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.PlayerData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.Utils
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/** Represents a module handling auto-refill mechanics within the system. */
class AutoRefillModule : ModuleInterface {
    override fun enabled(): Boolean = Config.AutoRefillModule.ENABLED

    @Suppress("UnstableApiUsage")
    override fun cmd(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("autorefill")
                .requires { it.sender.hasPermission(Perms.AutoRefill.USE) }
                .executes { it -> Utils.tryCatch(it) { toggle(it.sender as Player) } })
    }

    private val cooldowns = ConcurrentHashMap<UUID, Long>()
    private val cooldownMs = 250L
    private val offHandSlot = 40

    init {
        if (enabled()) {
            PlayerData.createTable()
            instance.server.scheduler.runTaskTimer(
                instance,
                Runnable { cooldowns.entries.removeIf { System.currentTimeMillis() - it.value > cooldownMs * 2 } },
                1L * 20L * 60L,
                5L * 20L * 60L
            )
            Runtime.getRuntime().addShutdownHook(Thread { synchronized(this, cooldowns::clear) })
        }
    }

    /**
     * Handles the PlayerItemConsumeEvent.
     * @param event the event to handle.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerItemConsumeEvent) {
        if (!canAttemptRefill(event.player)) return
        attemptRefill(event.getPlayer())
    }

    /**
     * Refills the player's main hand and offhand slots when they place a block.
     * @param event the BlockPlaceEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockPlaceEvent) {
        if (!canAttemptRefill(event.player)) return
        attemptRefill(event.getPlayer())
    }

    /**
     * Handles the PlayerInteractEvent to refill the player's main hand and offhand slots.
     * @param event the PlayerInteractEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (!canAttemptRefill(event.player)) return
        attemptRefill(event.getPlayer())
    }

    /**
     * Checks if the player can attempt to refill.
     * @param player the player to check.
     * @return true if the player can attempt to refill.
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
     * Attempts to refill the player's main hand and offhand slots.
     * @param player the player to refill.
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
     * Finds the best slot to refill the target slot.
     * @param inventory the player's inventory.
     * @param material the material to refill.
     * @param currentSlot the current slot.
     * @return the best slot to refill from.
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
     * Refills the target slot with the source slot.
     * @param inventory the player's inventory.
     * @param source the source slot.
     * @param target the target slot.
     * @param itemStack the item to refill.
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
     * A function to move bowls and bottles in an inventory.
     * @param inv The inventory to move the bowls and bottles in.
     * @param slot The slot to move the bowls and bottles from.
     * @return True if the bowls and bottles were moved successfully, false otherwise.
     */
    private fun moveBowlsAndBottles(inv: Inventory, slot: Int): Boolean {
        val itemStack = inv.getItem(slot) ?: return false
        if (!MaterialRegistry.BOWL_OR_BOTTLE.contains(itemStack.type)) return false

        inv.clear(slot)

        val leftovers = inv.addItem(itemStack)
        if (inv.getItem(slot)?.amount == null ||
            inv.getItem(slot)?.amount == 0 ||
            inv.getItem(slot)?.type == Material.AIR
        ) return true

        if (leftovers.isNotEmpty()) {
            val holder = inv.holder
            if (holder !is Player) return false
            for (leftover in leftovers.values) {
                holder.world.dropItem(holder.location, leftover)
            }
            return false
        }

        for (i in 35 downTo 0) {
            if (inv.getItem(i)?.amount == null ||
                inv.getItem(i)?.amount == 0 ||
                inv.getItem(i)?.type == Material.AIR
            ) {
                inv.setItem(i, itemStack)
                return true
            }
        }
        return false
    }

    /**
     * Checks if AutoRefill is enabled for the given player.
     * @param player the player to check.
     * @return true if enabled (default), false if explicitly disabled.
     */
    private fun isEnabledForPlayer(player: Player): Boolean {
        return PlayerData.getData().firstOrNull { it.id == player.uniqueId.toString() }?.autorefill ?: true
    }

    /**
     * Retrieves the PlayerData object for the given player.
     * @param player The player to check.
     * @return The PlayerData object, or a default object with both fields set to true if not found.
     */
    private fun getPlayerData(player: Player): PlayerData {
        return PlayerData.getData().firstOrNull { it.id == player.uniqueId.toString() }
            ?: PlayerData(id = player.uniqueId.toString(), autorefill = true, autotool = true)
    }

    /**
     * Toggles AutoRefill for the given player.
     * @param player the player to toggle.
     */
    private fun toggle(player: Player) {
        val playerData = getPlayerData(player)
        val updatedData = playerData.copy(autorefill = !(playerData.autorefill ?: false))
        PlayerData.setData(updatedData)
        cooldowns.remove(player.uniqueId)
        player.sendActionBar(("${"AutoRefill:".fireFmt()} ${if (isEnabledForPlayer(player)) "<green>ON" else "<red>OFF"}").mm())
    }
}