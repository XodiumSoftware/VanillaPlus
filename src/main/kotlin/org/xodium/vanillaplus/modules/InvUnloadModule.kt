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
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ChestAccessManager
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.managers.CooldownManager
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.Utils

/** Represents a module handling inv-unload mechanics within the system. */
class InvUnloadModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.invUnloadModule.enabled

    private val permPrefix: String = "${instance::class.simpleName}.invunload".lowercase()

    @Suppress("UnstableApiUsage")
    override fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("invunload")
                .requires { it.sender.hasPermission(perms()[1]) }
                .executes { it -> Utils.tryCatch(it) { unload(it.sender as Player) } }
        )
    }

    override fun perms(): List<Permission> {
        return listOf(
            Permission(
                "$permPrefix.use",
                "Allows use of the autorestart command",
                PermissionDefault.OP
            )
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerQuitEvent) {
        if (!enabled()) return

        val uuid = event.player.uniqueId
        Utils.lastUnloads.remove(uuid)
        Utils.activeVisualizations.remove(uuid)
    }

    /**
     * Unloads the inventory of the specified player.
     * @param player The player whose inventory to unload.
     */
    private fun unload(player: Player) {
        val cooldownKey = NamespacedKey(instance, "invunload_cooldown")
        val cooldownDuration = ConfigManager.data.invUnloadModule.cooldown
        if (CooldownManager.isOnCooldown(player, cooldownKey, cooldownDuration)) {
            player.sendActionBar("You must wait before using this again.".fireFmt().mm())
            return
        }
        CooldownManager.setCooldown(player, cooldownKey, System.currentTimeMillis())

        val startSlot = 9
        val endSlot = 35
        val chests = Utils.findBlocksInRadius(player.location, 5)
        if (chests.isEmpty()) {
            player.sendActionBar("No chests found nearby".fireFmt().mm())
            return
        }

        val deniedChestKey = NamespacedKey(instance, "denied_chest")
        val useableChests = chests.filter { ChestAccessManager.isAllowed(player, deniedChestKey, it) }
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
        }

        player.playSound(ConfigManager.data.invUnloadModule.soundOnUnload.toSound(), Sound.Emitter.self())
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