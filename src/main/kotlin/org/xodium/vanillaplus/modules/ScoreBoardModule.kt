package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.completedAdvancements
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt

/** Represents a module handling scoreboard mechanics within the system. */
internal class ScoreBoardModule : ModuleInterface<ScoreBoardModule.Config> {
    override val config: Config = Config()

    private val advancementObjective = "advancements"

    @EventHandler
    fun on(event: PlayerAdvancementDoneEvent) {
        if (!enabled()) return
        scoreboard()
    }

    private fun scoreboard() {
        val board = instance.server.scoreboardManager.mainScoreboard
        val objective = getOrCreateObjective(board)
        instance.server.onlinePlayers.forEach {
            objective.getScore(it.name).score = it.completedAdvancements.size
            it.scoreboard = board
        }
    }

    private fun getOrCreateObjective(board: Scoreboard): Objective =
        board.getObjective(advancementObjective) ?: board
            .registerNewObjective(
                advancementObjective,
                Criteria.DUMMY,
                config.scoreboardTitle.mm(),
            ).apply { displaySlot = config.scoreboardDisplaySlot }

    data class Config(
        override var enabled: Boolean = true,
        var scoreboardTitle: String = "Advancements".fireFmt(),
        var scoreboardDisplaySlot: DisplaySlot = DisplaySlot.SIDEBAR,
    ) : ModuleInterface.Config
}
