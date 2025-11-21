@file:Suppress("ktlint:standard:no-wildcard-imports")

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
import java.util.*
import java.util.concurrent.CompletableFuture

/** Represents a module handling locator mechanics within the system. */
internal class LocatorModule : ModuleInterface<ModuleInterface.Config> {
    private val colors = NamedTextColor.NAMES.keys().map { it.toString() } + listOf("<RRGGBB>", "reset")

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("locator")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .then(
                        Commands
                            .argument("color", ArgumentTypes.namedColor())
                            .suggests { _, builder ->
                                colors
                                    .filter { it.startsWith(builder.remaining.lowercase()) }
                                    .forEach(builder::suggest)
                                CompletableFuture.completedFuture(builder.build())
                            }.executes { ctx ->
                                ctx.tryCatch {
                                    if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                                    val player = it.sender as Player
                                    val color = ctx.getArgument("color", NamedTextColor::class.java)
                                    locator(player, colour = color)
                                }
                            },
                    ).then(
                        Commands
                            .argument("hex", ArgumentTypes.hexColor())
                            .executes { ctx ->
                                ctx.tryCatch {
                                    if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                                    val player = it.sender as Player
                                    val hex = ctx.getArgument("hex", TextColor::class.java)
                                    locator(player, hex = hex)
                                }
                            },
                    ).then(
                        Commands
                            .literal("reset")
                            .executes { ctx ->
                                ctx.tryCatch {
                                    if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                                    val player = it.sender as Player
                                    locator(player)
                                }
                            },
                    ),
                "Allows players to personalise their locator bar",
                listOf("lc"),
            ),
        )

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.locator".lowercase(),
                "Allows use of the locator command",
                PermissionDefault.TRUE,
            ),
        )

    /**
     * Modifies the colour of a player's waypoint based on the specified parameters.
     * @param player The player whose waypoint is being modified.
     * @param colour The optional named colour to apply to the waypoint.
     * @param hex The optional hex colour to apply to the waypoint.
     */
    private fun locator(
        player: Player,
        colour: NamedTextColor? = null,
        hex: TextColor? = null,
    ) {
        val cmd = "waypoint modify ${player.name}"

        when {
            colour != null -> instance.server.dispatchCommand(player, "$cmd color $colour")

            hex != null ->
                instance.server.dispatchCommand(
                    player,
                    "$cmd color hex ${String.format(Locale.ENGLISH, "%06X", hex.value())}",
                )

            else -> instance.server.dispatchCommand(player, "$cmd color reset")
        }
    }
}
