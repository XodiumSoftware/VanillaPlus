@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.advancement.AdvancementProgress
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import java.util.*

/** Represents a module handling scoreboard mechanics within the system. */
internal class ScoreBoardModule : ModuleInterface<ScoreBoardModule.Config> {
    override val config: Config = Config()

    private val activeScoreboards = mutableMapOf<UUID, Scoreboard>()

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("scoreboard")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx -> ctx.tryCatch { scoreboard(it.sender as Player) } },
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

    @EventHandler
    fun on(event: PlayerQuitEvent) {
        if (!enabled()) return
        cleanup(event.player)
    }

    /**
     * Creates and displays a custom scoreboard for the specified player.
     * @param player The player to display the scoreboard to.
     */
    private fun scoreboard(player: Player) {
        val scoreboard = activeScoreboards[player.uniqueId] ?: instance.server.scoreboardManager.newScoreboard

        scoreboard.objectives.forEach { it.unregister() }

        val objective =
            scoreboard.registerNewObjective(
                "${instance::class.simpleName}.scoreboard.${System.currentTimeMillis()}",
                Criteria.DUMMY,
                "<b>Advancements</b>".fireFmt().mm(),
            )

        objective.displaySlot = DisplaySlot.SIDEBAR

        val leaderboard =
            instance.server.onlinePlayers
                .map { player ->
                    val count = player.getAdvancements().count { it.isDone }
                    Pair(player.displayName(), count)
                }.sortedByDescending { it.second }
                .take(10)

        leaderboard.forEachIndexed { index, (name, count) ->
            val position = index + 1
            val line =
                when (position) {
                    1 -> "$name $count"
                    2 -> "$name $count"
                    3 -> "$name $count"
                    else -> "$name $count"
                }
            objective.getScore(line).score = 10 - index
        }

        activeScoreboards[player.uniqueId] = scoreboard
        player.scoreboard = scoreboard
    }

    /**
     * Gets a list of all advancement progress tracking objects for this player.
     * @return List of [AdvancementProgress] objects representing the player's progress
     *         for every advancement available on the server
     */
    private fun Player.getAdvancements(): List<AdvancementProgress> =
        instance.server
            .advancementIterator()
            .asSequence()
            .map { getAdvancementProgress(it) }
            .toList()

    /**
     * Cleans up scoreboard resources for the specified player.
     * @param player The player whose scoreboard should be cleaned up.
     */
    private fun cleanup(player: Player) {
        activeScoreboards.remove(player.uniqueId)?.let { it.objectives.forEach { obj -> obj.unregister() } }
        player.scoreboard = instance.server.scoreboardManager.mainScoreboard
    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
