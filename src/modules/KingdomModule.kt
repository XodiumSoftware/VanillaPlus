package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.KingdomData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.DatabaseManager
import org.xodium.vanillaplus.utils.CommandUtils.executesCatching
import org.xodium.vanillaplus.utils.Utils.MM
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.toKotlinUuid

/** Represents a module handling kingdom mechanics within the system. */
@OptIn(ExperimentalUuidApi::class)
internal object KingdomModule : ModuleInterface {
    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("kingdom")
                    .then(
                        Commands
                            .literal("grant")
                            .requires { it.sender.hasPermission(perms[0]) }
                            .then(
                                Commands
                                    .argument("player", ArgumentTypes.player())
                                    .executesCatching { ctx ->
                                        val target =
                                            ctx
                                                .getArgument(
                                                    "player",
                                                    PlayerSelectorArgumentResolver::class.java,
                                                ).resolve(ctx.source)
                                                .single()
                                        grantKingdom(ctx.source.sender as? Player, target)
                                    },
                            ),
                    ),
                "Manage kingdoms",
                listOf("k"),
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.kingdom.grant".lowercase(),
                "Allows granting a kingdom to a player",
                PermissionDefault.OP,
            ),
        )

    /**
     * Grants a kingdom to [target], notifying both sender and target.
     * The kingdom name is derived from [target]'s username.
     * @param sender The player who ran the command, or `null` if run from console.
     * @param target The player receiving the kingdom.
     */
    private fun grantKingdom(
        sender: Player?,
        target: Player,
    ) {
        val kingdomName = "${target.name} Kingdom"

        DatabaseManager.setKingdom(KingdomData(owner = target.uniqueId.toKotlinUuid(), name = kingdomName))

        target.sendMessage(
            MM.deserialize(
                Config.KingdomMessages.grantedTarget,
                Placeholder.unparsed("kingdom", kingdomName),
            ),
        )

        val senderMsg =
            MM.deserialize(
                Config.KingdomMessages.grantedSender,
                Placeholder.unparsed("player", target.name),
                Placeholder.unparsed("kingdom", kingdomName),
            )

        sender?.sendMessage(senderMsg)
            ?: instance.logger.info("Granted kingdom '$kingdomName' to ${target.name}")
    }

    /** Represents the config of the module. */
    object Config {
        object KingdomMessages {
            var grantedTarget: String =
                "<gradient:#FFE259:#FFA751>You have been granted the kingdom <b><kingdom></b>!</gradient>"
            var grantedSender: String =
                "<gradient:#FFE259:#FFA751>Granted kingdom <b><kingdom></b> to <player>.</gradient>"
        }
    }
}
