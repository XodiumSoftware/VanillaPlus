/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.NicknameData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.managers.ModuleManager
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.Utils

class NicknameModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.nicknameModule.enabled

    @Suppress("UnstableApiUsage")
    override fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("nickname")
                .requires { it.sender.hasPermission(perms()[0]) }
                .then(
                    Commands.argument("name", StringArgumentType.greedyString())
                        .executes { ctx ->
                            Utils.tryCatch(ctx) {
                                nickname(it.sender as Player, StringArgumentType.getString(ctx, "name"))
                            }
                        }
                )
        )
    }

    override fun perms(): List<Permission> {
        return listOf(
            Permission(
                "${instance::class.simpleName}.nickname.use".lowercase(),
                "Allows use of the nickname command",
                PermissionDefault.TRUE
            )
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return
        val nickname = NicknameData.get(event.player.uniqueId)
        if (nickname != null) event.player.displayName(nickname.mm())
    }

    /**
     * Sets the nickname of the player to the given name.
     * @param player The player whose nickname is to be set.
     * @param name The new nickname for the player.
     */
    private fun nickname(player: Player, name: String) {
        if (name.isBlank()) {
            NicknameData.remove(player.uniqueId)
            player.displayName(player.name.mm())
        } else {
            NicknameData.set(player.uniqueId, name)
            player.displayName(name.mm())
        }
        ModuleManager.tabListModule.updatePlayerDisplayName(player)
    }
}