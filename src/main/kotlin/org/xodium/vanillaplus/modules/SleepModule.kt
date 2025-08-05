package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerBedEnterEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import kotlin.math.ceil

/** Represents a module handling sleeping mechanics within the system. */
internal class SleepModule : ModuleInterface<SleepModule.Config> {
    override val config: Config = Config()

    companion object {
        private const val FULL_PERCENTAGE: Double = 100.0
    }

    @EventHandler
    fun on(event: PlayerBedEnterEvent) {
        val world = event.player.world
        if (!enabled() ||
            event.bedEnterResult != PlayerBedEnterEvent.BedEnterResult.OK ||
            world.isDayTime
        ) {
            return
        }

        instance.server.scheduler.runTask(
            instance,
            Runnable {
                val players = world.players
                if (players.isEmpty()) return@Runnable
                if (shouldSkipNight(players.count { it.isSleeping }, players.size)) world.time = 0
            },
        )
    }

    /**
     * Determines whether the night should be skipped based on the number of players sleeping
     * and the required percentage of sleeping players.
     * @param sleepingPlayers The number of players currently sleeping.
     * @param totalPlayers The total number of players in the world.
     * @return True if the number of sleeping players meets or exceeds the threshold
     *         required to skip the night, otherwise false.
     */
    private fun shouldSkipNight(
        sleepingPlayers: Int,
        totalPlayers: Int,
    ): Boolean = sleepingPlayers >= ceil(totalPlayers * config.sleepPercentage / FULL_PERCENTAGE)

    data class Config(
        override var enabled: Boolean = true,
        var sleepPercentage: Int = 51,
    ) : ModuleInterface.Config
}
