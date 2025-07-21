package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerBedEnterEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import kotlin.math.ceil

/** Represents a module handling sleeping mechanics within the system. */
internal class SleepModule : ModuleInterface<SleepModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    @EventHandler
    fun on(event: PlayerBedEnterEvent) {
        val world = event.player.world
        if (!enabled()
            || event.bedEnterResult != PlayerBedEnterEvent.BedEnterResult.OK
            || world.isDayTime
        ) return

        instance.server.scheduler.runTask(instance, Runnable {
            val players = world.players
            if (players.isEmpty()) return@Runnable
            if (players.count { it.isSleeping } >= ceil(players.size * config.sleepPercentage / 100.0)) {
                world.time = 0
            }
        })
    }

    data class Config(
        override var enabled: Boolean = true,
        var sleepPercentage: Int = 51,
    ) : ModuleInterface.Config
}