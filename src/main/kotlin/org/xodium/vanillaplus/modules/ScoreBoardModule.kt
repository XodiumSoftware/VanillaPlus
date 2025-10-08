package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.PlayerData
import org.xodium.vanillaplus.interfaces.ModuleInterface
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

    @EventHandler
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return
        val player = event.player
        val playerData = PlayerData.get(player)
        if (playerData?.scoreboardVisibility == true) {
            player.scoreboard = instance.server.scoreboardManager.mainScoreboard
        } else {
            player.scoreboard = instance.server.scoreboardManager.newScoreboard
        }
    }

    /**
     * Toggles the display of the scoreboard sidebar for a player.
     * @param player The player whose scoreboard sidebar should be toggled.
     */
    private fun toggle(player: Player) {
        val playerData = PlayerData.get(player)
        if (playerData?.scoreboardVisibility == false) {
            player.scoreboard = instance.server.scoreboardManager.mainScoreboard
            PlayerData.set(player, scoreboardVisibility = true)
        } else {
            player.scoreboard = instance.server.scoreboardManager.newScoreboard
            PlayerData.set(player, scoreboardVisibility = false)
        }
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
