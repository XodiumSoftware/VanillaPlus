/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.sound.Sound
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.hooks.ChestSortHook
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.Utils

/** Represents a module handling inv-unload mechanics within the system. */
class InvUnloadModule : ModuleInterface {
    override fun enabled(): Boolean = Config.InvUnloadModule.ENABLED

    @Suppress("UnstableApiUsage")
    override fun cmd(): LiteralArgumentBuilder<CommandSourceStack>? {
        return Commands.literal("invunload")
            .requires { it.sender.hasPermission(Perms.InvUnload.USE) }
            .executes { it -> Utils.tryCatch(it) { unload(it.sender as Player) } }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerQuitEvent) {
        if (enabled()) {
            val uuid = event.player.uniqueId
            Utils.lastUnloads.remove(uuid)
            Utils.activeVisualizations.remove(uuid)
        }
    }

    /**
     * Unloads the inventory of the specified player.
     * @param player The player whose inventory to unload.
     */
    private fun unload(player: Player) {
        if (!Utils.cooldown(
                player,
                Config.InvUnloadModule.COOLDOWN,
                NamespacedKey(instance, "${InvUnloadModule::class.simpleName?.lowercase()}_cooldown")
            )
        ) return

        val startSlot = 9
        val endSlot = 35
        val chests = Utils.findBlocksInRadius(player.location, 5)
        if (chests.isEmpty()) {
            player.sendActionBar("No chests found nearby".fireFmt().mm())
            return
        }

        val useableChests = chests.filter { Utils.canPlayerUseChest(it, player) }
        if (useableChests.isEmpty()) {
            player.sendActionBar("No usable chests found nearby".fireFmt().mm())
            return
        }

        val affectedChests = mutableListOf<Block>()
        for (block in useableChests) {
            val inv = (block.state as Container).inventory
            if (stuffInventoryIntoAnother(player, inv, true, startSlot, endSlot)) {
                affectedChests.add(block)
            }
        }

        if (affectedChests.isEmpty()) {
            player.sendActionBar("No items were unloaded".fireFmt().mm())
            return
        }

        player.sendActionBar("Inventory unloaded".mangoFmt().mm())
        Utils.lastUnloads[player.uniqueId] = affectedChests

        for (block in affectedChests) {
            Utils.chestEffect(player, block)
            Utils.laserEffect(player)
            if (ChestSortHook.shouldSort(player)) ChestSortHook.sort(block)
        }

        player.playSound(Config.InvUnloadModule.SOUND_ON_UNLOAD, Sound.Emitter.self())
    }

    /**
     * Moves items from the player's inventory to another inventory.
     * @param player The player whose inventory is being moved.
     * @param destination The destination inventory to move items into.
     * @param onlyMatchingStuff If true, only moves items that match the destination's contents.
     * @param startSlot The starting slot in the player's inventory to move items from.
     * @param endSlot The ending slot in the player's inventory to move items from.
     * @return True if items were moved, false otherwise.
     */
    private fun stuffInventoryIntoAnother(
        player: Player,
        destination: Inventory,
        onlyMatchingStuff: Boolean,
        startSlot: Int,
        endSlot: Int,
    ): Boolean {
        val source = player.inventory
        val initialCount = countInventoryContents(source)
        var moved = false

        for (i in startSlot..endSlot) {
            val item = source.getItem(i) ?: continue
            if (Tag.SHULKER_BOXES.isTagged(item.type) && destination.holder is ShulkerBox) continue
            if (onlyMatchingStuff && !Utils.doesChestContain(destination, item)) continue

            val leftovers = destination.addItem(item)
            val movedAmount = item.amount - leftovers.values.sumOf { it.amount }
            if (movedAmount > 0) {
                moved = true
                source.clear(i)
                leftovers.values.firstOrNull()?.let { source.setItem(i, it) }
                destination.location?.let { Utils.protocolUnload(it, item.type, movedAmount) }
            }
        }
        return moved && initialCount != countInventoryContents(source)
    }

    /**
     * Counts the total number of items in the given inventory.
     * @param inv The inventory to count items in.
     * @return The total number of items in the inventory.
     */
    private fun countInventoryContents(inv: Inventory): Int = inv.contents.filterNotNull().sumOf { it.amount }

}