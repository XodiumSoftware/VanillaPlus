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

    private val colors = listOf(
        "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
        "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
        "yellow", "white", "hex", "reset"
    )

    override fun enabled(): Boolean = config.enabled

    override fun cmds(): CommandData? {
        return CommandData(
            listOf(
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
                                    locator(
                                        (it.sender as Player),
                                        colour = ctx.getArgument("color", NamedTextColor::class.java)
                                    )
                                }
                            }
                    )
                    .then(
                        Commands.argument("hex", ArgumentTypes.hexColor())
                            .suggests { ctx, builder ->
                                if (builder.remaining.isEmpty()) builder.suggest("<#RRGGBB>")
                                CompletableFuture.completedFuture(builder.build())
                            }
                            .executes { ctx ->
                                ctx.tryCatch {
                                    locator(
                                        (it.sender as Player),
                                        hex = ctx.getArgument("hex", TextColor::class.java)
                                    )
                                }
                            }
                    )
                    .then(
                        Commands.literal("reset")
                            .executes { ctx -> ctx.tryCatch { locator((it.sender as Player)) } }
                    )
            ),
            "Allows players to personalise their locator bar.",
            listOf("lc")
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
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}