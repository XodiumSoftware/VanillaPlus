package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.scoreboard.DisplaySlot
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.prefix
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch

/** Represents a module handling scoreboard mechanics within the system. */
internal class ScoreBoardModule : ModuleInterface<ScoreBoardModule.Config> {
    override val config: Config = Config()

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("leaderboard")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx ->
                        ctx.tryCatch {
                            if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                            toggle(it.sender as Player)
                        }
                    },
                "This command allows you to open the leaderboard",
                listOf("lb", "board"),
            ),
        )

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance::class.simpleName}.leaderboard".lowercase(),
                "Allows use of the leaderboard command",
                PermissionDefault.TRUE,
            ),
        )

    /**
     * Toggles the display of the scoreboard sidebar for a player.
     * @param player The player whose scoreboard sidebar should be toggled.
     */
    private fun toggle(player: Player) {
        val scoreboard = player.scoreboard
        val sidebar = scoreboard.getObjective(DisplaySlot.SIDEBAR)
        if (sidebar != null) {
            scoreboard.clearSlot(DisplaySlot.SIDEBAR)
        } else {
            val objective = scoreboard.getObjective(config.scoreboardObjective)
            if (objective == null) {
                instance.logger.warning("Scoreboard objective '${config.scoreboardObjective}' not found for player ${player.name}!")
                player.sendMessage("${instance.prefix} <red>Error Occurred, Check Console!".mm())
                return
            }
            objective.displaySlot = DisplaySlot.SIDEBAR
        }
    }

    data class Config(
        override var enabled: Boolean = true,
        var scoreboardObjective: String = "bac_advancements",
    ) : ModuleInterface.Config
}
