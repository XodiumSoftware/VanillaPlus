/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.builder.item.ItemBuilder
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
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.Utils.moveBowlsAndBottles
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface

class AutoRefillModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.AutoRefillModule().enabled

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerItemConsumeEvent) = attemptRefill(event.getPlayer())

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: BlockPlaceEvent) = attemptRefill(event.getPlayer())

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerInteractEvent) = attemptRefill(event.getPlayer())

    private fun attemptRefill(player: Player) {
        attemptRefill(player, true)
        attemptRefill(player, false)
    }

    private fun attemptRefill(player: Player, offHand: Boolean) {
        val inventory = player.inventory
        val item = if (offHand) inventory.itemInOffHand else inventory.itemInMainHand
        val material = item.type
        val heldItemSlot = inventory.heldItemSlot

        if (item.amount != 1) return
        if (!player.hasPermission(Perms.AutoRefill.USE)) return
        TODO("check database if enabled")

        val refillSlot =
            getMatchingStackPosition(inventory, material, if (offHand) 45 else inventory.heldItemSlot)
        if (refillSlot != -1) {
            refillStack(inventory, refillSlot, if (offHand) 40 else heldItemSlot, inventory.getItem(refillSlot))
        }
    }

    fun getMatchingStackPosition(inventory: PlayerInventory, material: Material, currentSlot: Int): Int {
        var bestSlot = -1
        var smallestAmount = 65
        for (i in 0..<36) {
            if (i == currentSlot) continue
            val item = inventory.getItem(i) ?: continue
            if (item.type != material) continue
            if (item.amount == 64) return i
            if (item.amount < smallestAmount) {
                smallestAmount = item.amount
                bestSlot = i
            }
        }
        return bestSlot
    }

    fun refillStack(inventory: Inventory, source: Int, target: Int, itemStack: ItemStack?) {
        instance.server.scheduler.runTask(instance, Runnable {
            when {
                inventory.getItem(source) == null -> return@Runnable
                inventory.getItem(source) != itemStack -> return@Runnable
                inventory.getItem(target) != null && !moveBowlsAndBottles(
                    inventory,
                    target
                ) -> return@Runnable

                else -> {
                    inventory.setItem(source, null)
                    inventory.setItem(target, itemStack)
                }
            }
        })
    }

    val guiItem = ItemBuilder.from(Material.MILK_BUCKET)
        .name(Utils.mangoFormat("AutoRefill").mm())
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to toggle <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Auto refills your items when empty",
                "   <gray>Shortcut: <gold>/autorefill | /ar"
            ).mm()
        )
        .asGuiItem { player, _ -> toggle(player) }

    fun toggle(player: Player) {
        TODO("toggle in database")
    }
}