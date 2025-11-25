package org.xodium.vanillaplus.managers

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.prefix
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch

/** Represents the module manager within the system. */
internal object ModuleManager {
    private val configCmd =
        CommandData(
            Commands
                .literal("vanillaplus")
                .then(
                    Commands
                        .literal("reload")
                        .requires { it.sender.hasPermission(configPerm) }
                        .executes { ctx ->
                            ctx.tryCatch {
                                // TODO: run update command.
                                if (it.sender is Player) {
                                    it.sender.sendMessage("${instance.prefix} <green>Config reloaded successfully".mm())
                                }
                            }
                        },
                ),
            "Main VanillaPlus command. Use subcommands for actions.",
            listOf("vp"),
        )

    private val configPerm =
        Permission(
            "${instance.javaClass.simpleName}.reload".lowercase(),
            "Allows use of the vanillaplus reload command",
            PermissionDefault.OP,
        )
}
