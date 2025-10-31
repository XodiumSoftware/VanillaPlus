package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import kotlinx.serialization.Serializable
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch

/** Represents a module handling sign mechanics within the system. */
internal class SignModule : ModuleInterface<SignModule.Config> {
    override val config: Config = Config()

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("sign")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .then(
                        Commands
                            .argument("line", IntegerArgumentType.integer(1, 4))
                            .suggests { _, builder ->
                                (1..4).forEach(builder::suggest)
                                builder.buildFuture()
                            }.then(
                                Commands
                                    .argument("text", StringArgumentType.greedyString())
                                    .executes { ctx ->
                                        ctx.tryCatch {
                                            val player =
                                                it.sender as? Player
                                                    ?: instance.logger.warning("Command can only be executed by a Player!")
                                            val line = IntegerArgumentType.getInteger(ctx, "line") - 1
                                            val text = StringArgumentType.getString(ctx, "text")

                                            sign(player as Player, line, text)
                                        }
                                    },
                            ),
                    ),
                "Edits the content of the sign",
                listOf("s"),
            ),
        )

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance::class.simpleName}.signedit".lowercase(),
                "Allows use of the signedit command",
                PermissionDefault.TRUE,
            ),
        )

    private fun sign(
        player: Player,
        line: Int,
        text: String,
    ) {
        val target = player.getTargetBlockExact(5)

        if (target == null || target.state !is Sign) return

        val sign = target.state as Sign
        val signSide = sign.getSide(sign.getInteractableSideFor(player))

        signSide.line(line, text.mm())
        sign.update()
    }

    @Serializable
    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
