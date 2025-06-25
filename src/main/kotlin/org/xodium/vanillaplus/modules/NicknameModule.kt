/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.NicknameData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.Utils

class NicknameModule(private val tabListModule: TabListModule) : ModuleInterface<NicknameModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled && tabListModule.enabled()

    override fun cmds(): CommandData? {
        return CommandData(
            listOf(
                @Suppress("UnstableApiUsage")
                Commands.literal("nickname")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx -> Utils.tryCatch(ctx) { nickname(it.sender as Player, "") } }
                    .then(
                        Commands.argument("name", StringArgumentType.greedyString())
                            .executes { ctx ->
                                Utils.tryCatch(ctx) {
                                    nickname(it.sender as Player, StringArgumentType.getString(ctx, "name"))
                                }
                            }
                    )
            ),
            "Allows players to set or remove their nickname.",
            listOf("nick")
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
        tabListModule.updatePlayerDisplayName(player)
    }

    data class Config(
        override val enabled: Boolean = true
    ) : ModuleInterface.Config
}