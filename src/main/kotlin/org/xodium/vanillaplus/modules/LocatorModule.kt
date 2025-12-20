@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import java.util.concurrent.CompletableFuture

/** Represents a module handling locator mechanics within the system. */
internal object LocatorModule : ModuleInterface {
    private val colors = NamedTextColor.NAMES.keys().map { it.toString() } + listOf("<RRGGBB>", "reset")

    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("locator")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .then(
                        Commands
                            .argument("color", ArgumentTypes.namedColor())
                            .suggests { _, builder ->
                                colors
                                    .filter { it.startsWith(builder.remaining.lowercase()) }
                                    .forEach(builder::suggest)
                                CompletableFuture.completedFuture(builder.build())
                            }.playerExecuted { player, ctx ->
                                locator(player, colour = ctx.getArgument("color", NamedTextColor::class.java))
                            },
                    ).then(
                        Commands
                            .argument("hex", ArgumentTypes.hexColor())
                            .playerExecuted { player, ctx ->
                                locator(player, hex = ctx.getArgument("hex", TextColor::class.java))
                            },
                    ).then(
                        Commands
                            .literal("reset")
                            .playerExecuted { player, _ -> locator(player) },
                    ),
                "Allows players to personalise their locator bar",
                listOf("lc"),
            ),
        )

    override val perms =
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
        when {
            colour != null -> player.waypointColor = Color.fromRGB(colour.value())
            hex != null -> player.waypointColor = Color.fromRGB(hex.value())
            else -> player.waypointColor = null
        }
    }

    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
