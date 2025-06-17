/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.GameMode
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.Utils

/** Represents a module handling trowel mechanics within the system. */
class TrowelModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.trowelModule.enabled

    @Suppress("UnstableApiUsage")
    override fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("trowel")
                .requires { it.sender.hasPermission(perms()[0]) }
                .executes { ctx -> Utils.tryCatch(ctx) { toggle(it.sender as Player) } })
    }

    override fun perms(): List<Permission> {
        return listOf(
            Permission(
                "${instance::class.simpleName}.trowel.toggle".lowercase(),
                "Allows use of the trowel give command",
                PermissionDefault.TRUE
            )
        )
    }

    private val activePlayers = mutableSetOf<Player>()

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (!enabled() || event.hand != EquipmentSlot.HAND || event.action != Action.RIGHT_CLICK_BLOCK) return

        val player = event.player

        if (player !in activePlayers) return

        event.isCancelled = true

        val inventory = player.inventory
        val slot = (0..8)
            .filter { inventory.getItem(it)?.type?.isBlock == true }
            .randomOrNull() ?: return
        val stack = inventory.getItem(slot) ?: return
        val blockType = stack.type
        val target = event.clickedBlock
            ?.getRelative(event.blockFace)
            ?.takeIf(Block::isEmpty) ?: return

        target.type = blockType

        if (player.gameMode != GameMode.CREATIVE) {
            stack.amount--
            if (stack.amount == 0) inventory.setItem(slot, null)
        }
    }

    /**
     * Toggles the trowel mode for the specified player.
     * @param player The player whose trowel mode is to be toggled.
     */
    private fun toggle(player: Player) {
        if (activePlayers.contains(player)) {
            activePlayers.remove(player)
            player.sendActionBar("Trowel mode disabled".fireFmt().mm())
        } else {
            activePlayers.add(player)
            player.sendActionBar("Trowel mode enabled".fireFmt().mm())
        }
    }
}