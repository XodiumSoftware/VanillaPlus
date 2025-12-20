@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
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

/** Represents a module handling locator mechanics within the system. */
internal object LocatorModule : ModuleInterface {
    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("locator")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .then(
                        Commands
                            .argument("color", ArgumentTypes.namedColor())
                            .playerExecuted { player, ctx ->
                                player.locator(ctx.getArgument("color", NamedTextColor::class.java))
                            },
                    ).then(
                        Commands
                            .argument("hex", ArgumentTypes.hexColor())
                            .playerExecuted { player, ctx ->
                                player.locator(ctx.getArgument("hex", TextColor::class.java))
                            },
                    ).then(
                        Commands
                            .literal("reset")
                            .playerExecuted { player, _ -> player.locator() },
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
     * @receiver Player The player whose waypoint colour is to be modified.
     * @param color The optional named colour to apply to the waypoint.
     */
    private fun Player.locator(color: TextColor? = null) {
        waypointColor = color?.let { Color.fromRGB(it.value()) }
        sendActionBar(Component.text("Locator color changed!", color))
    }

    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
