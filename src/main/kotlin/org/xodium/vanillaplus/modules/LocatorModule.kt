package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import java.util.concurrent.CompletableFuture

/** Represents a module handling locator mechanics within the system. */
class LocatorModule : ModuleInterface<LocatorModule.Config> {
    override val config: Config = Config()

    private val colors = NamedTextColor.NAMES.keys().map { it.toString() } + listOf("<RRGGBB>", "reset")

    override fun enabled(): Boolean = config.enabled

    override fun cmds(): List<CommandData> {
        return listOf(
            CommandData(
                Commands.literal("locator")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .then(
                        Commands.argument("color", ArgumentTypes.namedColor())
                            .suggests { ctx, builder ->
                                colors.filter { it.startsWith(builder.remaining.lowercase()) }
                                    .forEach(builder::suggest)
                                CompletableFuture.completedFuture(builder.build())
                            }
                            .executes { ctx ->
                                ctx.tryCatch {
                                    val player = ctx.source.sender as Player
                                    val color = ctx.getArgument("color", NamedTextColor::class.java)
                                    locator(player, colour = color)
                                }
                            }
                    )
                    .then(
                        Commands.argument("hex", ArgumentTypes.hexColor())
                            .executes { ctx ->
                                ctx.tryCatch {
                                    val player = ctx.source.sender as Player
                                    val hex = ctx.getArgument("hex", TextColor::class.java)
                                    locator(player, hex = hex)
                                }
                            }
                    )
                    .then(
                        Commands.literal("reset")
                            .executes { ctx ->
                                ctx.tryCatch {
                                    val player = ctx.source.sender as Player
                                    locator(player)
                                }
                            }
                    ),
                "Allows players to personalise their locator bar.",
                listOf("lc")
            )
        )
    }

    override fun perms(): List<Permission> {
        return listOf(
            Permission(
                "${instance::class.simpleName}.locator.use".lowercase(),
                "Allows use of the locator command",
                PermissionDefault.TRUE
            )
        )
    }

    /**
     * Modifies the colour of a player's waypoint based on the specified parameters.
     * If a `colour` is provided, sets the waypoint to the specified named colour.
     * If a `hex` is provided instead, sets the waypoint to the specified hex colour.
     * If neither is provided, resets the waypoint colour to default.
     *
     * @param player The player whose waypoint is being modified.
     * @param colour The optional named colour to apply to the waypoint.
     * @param hex The optional hex colour to apply to the waypoint.
     */
    private fun locator(player: Player, colour: NamedTextColor? = null, hex: TextColor? = null) {
        val cmd = "waypoint modify ${player.name}"
        when {
            colour != null -> instance.server.dispatchCommand(player, "$cmd color $colour")

            hex != null -> instance.server.dispatchCommand(
                player,
                "$cmd color hex ${String.format("%06X", hex.value())}"
            )

            else -> instance.server.dispatchCommand(player, "$cmd color reset")
        }
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}