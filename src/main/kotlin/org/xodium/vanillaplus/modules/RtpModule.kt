/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.utils.Utils

class RtpModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.rtpModule.enabled

    @Suppress("UnstableApiUsage")
    override fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("rtp")
                .requires { it.sender.hasPermission(perms()[0]) }
                .executes { ctx -> Utils.tryCatch(ctx) { rtp(it.sender as Player) } })
    }

    override fun perms(): List<Permission> {
        return listOf(
            Permission(
                "${instance::class.simpleName}.rtp.use".lowercase(),
                "Allows use of the rtp command",
                PermissionDefault.TRUE
            )
        )
    }

    private fun rtp(player: Player) {}
}