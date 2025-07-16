package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerBedEnterEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import kotlin.math.ceil

/** Represents a module handling sleeping mechanics within the system. */
class SleepModule : ModuleInterface<SleepModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    @EventHandler
    fun on(event: PlayerBedEnterEvent) {
        if (!enabled() || event.bedEnterResult != PlayerBedEnterEvent.BedEnterResult.OK) return

        val world = event.player.world
        if (world.isDayTime) return

        instance.server.scheduler.runTask(instance, Runnable {
            val onlinePlayers = world.players.size
            if (onlinePlayers == 0) return@Runnable

            val sleepingPlayers = world.players.count { it.isSleeping }
            val neededPlayers = ceil(onlinePlayers * (config.sleepPercentage / 100.0)).toInt()
            if (sleepingPlayers >= neededPlayers) world.time = 0
        })
    }

    data class Config(
        override var enabled: Boolean = true,
        var sleepPercentage: Int = 51,
    ) : ModuleInterface.Config
}