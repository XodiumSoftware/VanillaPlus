/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.GameMode
import org.bukkit.block.Block
import org.bukkit.block.data.Directional
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.PlayerData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt

/** Represents a module handling trowel mechanics within the system. */
class TrowelModule : ModuleInterface<TrowelModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    override fun cmds(): CommandData? {
        return CommandData(
            listOf(
                Commands.literal("trowel")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx -> ctx.tryCatch { toggle(it.sender as Player) } }
            ),
            "Allows players to toggle the trowel functionality.",
            emptyList()
        )
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (!enabled() || event.hand != EquipmentSlot.HAND || event.action != Action.RIGHT_CLICK_BLOCK) return

        val player = event.player

        if (!PlayerData.get(player).trowel) return

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
        val blockData = instance.server.createBlockData(blockType).also {
            if (it is Directional) it.facing = player.facing
        }

        target.blockData = blockData

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
        val playerData = PlayerData.get(player)
        val enabled = !playerData.trowel
        PlayerData.update(player, playerData.copy(trowel = enabled))
        val msg = if (enabled) "Trowel: <green>enabled" else "Trowel: <red>disabled"
        player.sendActionBar(msg.fireFmt().mm())
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}