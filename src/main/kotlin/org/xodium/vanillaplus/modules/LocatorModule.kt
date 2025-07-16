package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
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
                        Commands.argument("color", StringArgumentType.word())
                            .suggests { ctx, builder ->
                                colors.filter { it.startsWith(builder.remaining.lowercase()) }
                                    .forEach(builder::suggest)
                                CompletableFuture.completedFuture(builder.build())
                            }
                            .then(
                                Commands.argument("hex", StringArgumentType.word())
                                    .suggests { ctx, builder ->
                                        if (builder.remaining.isEmpty()) builder.suggest("<#RRGGBB>")
                                        CompletableFuture.completedFuture(builder.build())
                                    }
                                    .executes { ctx ->
                                        ctx.tryCatch {
                                            locator(hex = StringArgumentType.getString(ctx, "hex"))
                                        }
                                    }
                            )
                            .then(Commands.literal("reset").executes { ctx -> ctx.tryCatch { locator() } })
                            .executes { ctx ->
                                ctx.tryCatch {
                                    locator(color = StringArgumentType.getString(ctx, "color"))
                                }
                            }
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
     * Handles colour or hex-based logic for setting and resetting configurations.
     * @param color Optional named colour representation.
     * @param hex Optional hexadecimal color code representation.
     */
    private fun locator(color: String? = null, hex: String? = null) {
        if (color != null) {
            // colour handling (mc named colours)
        } else if (hex != null) {
            // hex handling
        } else {
            //TODO: reset mechanism
        }
    }

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}