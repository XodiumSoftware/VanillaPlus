package org.xodium.illyriaplus.commands

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import org.xodium.illyriaplus.data.CommandData
import org.xodium.illyriaplus.interfaces.CmdInterface
import org.xodium.illyriaplus.items.SceptreItem
import org.xodium.illyriaplus.utils.CommandUtils.playerExecuted
import kotlin.uuid.ExperimentalUuidApi

/**
 * Command to create a new kingdom and receive its sceptre.
 * Usage: /kingdom create <name>
 */
@OptIn(ExperimentalUuidApi::class)
internal object KingdomCmd : CmdInterface {
    override val cmds: Collection<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("kingdom")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .then(
                        Commands
                            .literal("create")
                            .playerExecuted { player, _ -> player.inventory.addItem(SceptreItem.item) },
                    ),
                "Create a new kingdom and receive its sceptre",
                listOf("k"),
            ),
        )

    override val perms: List<Permission> =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.kingdom".lowercase(),
                PermissionDefault.OP,
            ),
        )
}
