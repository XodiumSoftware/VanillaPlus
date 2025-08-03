package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch

/** Represents a module handling scoreboard mechanics within the system. */
internal class ScoreBoardModule : ModuleInterface<ScoreBoardModule.Config> {
    override val config: Config = Config()

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("scoreboard")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx -> ctx.tryCatch { scoreboard() } },
                "Shows the scoreboard",
                listOf("sb"),
            ),
        )

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance::class.simpleName}.scoreboard".lowercase(),
                "Allows use of the scoreboard command",
                PermissionDefault.TRUE,
            ),
        )

    private fun scoreboard() {
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
