package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling scoreboard mechanics within the system. */
internal class ScoreBoardModule : ModuleInterface<ScoreBoardModule.Config> {
    override val config: Config = Config()

//    @EventHandler
//    fun on(event: PlayerAdvancementDoneEvent) {
//        if (!enabled()) return
//        instance.server.onlinePlayers.forEach { it.scoreboard = instance.server.scoreboardManager.mainScoreboard }
//    }

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
