/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.TimeUtils
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/** Represents a module handling auto-restart mechanics within the system. */
class AutoRestartModule : ModuleInterface {
    override fun enabled(): Boolean = Config.AutoRestartModule.ENABLED

    init {
        if (enabled()) {
            instance.server.scheduler.runTaskTimerAsynchronously(
                instance,
                Runnable {
                    Config.AutoRestartModule.RESTART_TIMES.forEach {
                        if (isTimeToStartCountdown(it)) {
                            instance.server.scheduler.runTask(instance, Runnable { countdown() })
                        }
                    }
                },
                0L,
                TimeUtils.minutes(1)
            )
        }
    }

    /** Triggers a countdown for the server restart. */
    private fun countdown() {
        val totalMinutes = Config.AutoRestartModule.COUNTDOWN_START_MINUTES
        var remainingSeconds = totalMinutes * 60
        val totalSeconds = remainingSeconds
        val bossBar = Config.AutoRestartModule.BOSSBAR
        instance.server.onlinePlayers.forEach { player -> player.showBossBar(bossBar) }
        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable {
                if (remainingSeconds > 0) {
                    remainingSeconds--
                    val displayTime =
                        if (remainingSeconds % 60 > 0) (remainingSeconds / 60) + 1 else remainingSeconds / 60
                    bossBar.name(
                        Config.AutoRestartModule.BOSSBAR_NAME.replace(
                            "%t",
                            displayTime.toString()
                        ).mm()
                    )
                    val progress = remainingSeconds.toFloat() / totalSeconds
                    bossBar.progress(progress)
                    instance.server.onlinePlayers.forEach { player ->
                        if (!player.activeBossBars().contains(bossBar)) {
                            player.showBossBar(bossBar)
                        }
                    }
                } else {
                    instance.server.onlinePlayers.forEach { player -> player.hideBossBar(bossBar) }
                    instance.server.restart()
                }
            },
            0L,
            TimeUtils.seconds(1)
        )
    }

    /**
     * Returns true if the current time is equal to the time string in the plugin's configuration.
     * @param restartTime the time to compare to the current time.
     * @return true if the current time is equal to the restart time.
     */
    private fun isTimeToStartCountdown(restartTime: LocalTime): Boolean {
        return ChronoUnit.MINUTES.between(
            LocalTime.now().truncatedTo(ChronoUnit.MINUTES),
            restartTime
        ) == Config.AutoRestartModule.COUNTDOWN_START_MINUTES.toLong()
    }
}