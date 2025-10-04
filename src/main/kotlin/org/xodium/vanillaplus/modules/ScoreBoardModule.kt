package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch

/** Represents a module handling scoreboard mechanics within the system. */
internal class ScoreBoardModule : ModuleInterface<ScoreBoardModule.Config> {
    override val config: Config = Config()

    private val hiddenPlayers = mutableSetOf<Player>()

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

    @EventHandler
    fun on(event: PlayerQuitEvent) {
        if (!enabled()) return
        hiddenPlayers.remove(event.player)
    }

    /**
     * Toggles the display of the scoreboard sidebar for a player.
     * @param player The player whose scoreboard sidebar should be toggled.
     */
    private fun toggle(player: Player) {
        if (hiddenPlayers.contains(player)) {
            player.scoreboard = instance.server.scoreboardManager.mainScoreboard
            hiddenPlayers.remove(player)
        } else {
            player.scoreboard = instance.server.scoreboardManager.newScoreboard
            hiddenPlayers.add(player)
        }
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
