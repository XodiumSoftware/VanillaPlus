package org.xodium.illyriaplus.cmds

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import org.xodium.illyriaplus.Utils.Command.playerExecuted
import org.xodium.illyriaplus.data.CommandData
import org.xodium.illyriaplus.guis.AdminGui
import org.xodium.illyriaplus.interfaces.CmdInterface
import kotlin.uuid.ExperimentalUuidApi

/**
 * Command to open the kingdom admin GUI.
 * Usage: /kingdom
 */
@OptIn(ExperimentalUuidApi::class)
internal object KingdomCmd : CmdInterface {
    override val cmd: CommandData =
        CommandData(
            Commands
                .literal("kingdom")
                .requires { it.sender.hasPermission(perm) }
                .playerExecuted { player, _ -> AdminGui.window(player).open() },
            "Open the kingdom admin GUI",
            listOf("k"),
        )

    override val perm: Permission =
        Permission(
            "${instance.javaClass.simpleName}.kingdom".lowercase(),
            PermissionDefault.OP,
        )
}
