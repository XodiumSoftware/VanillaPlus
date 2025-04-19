/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.bossbar.BossBar
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.FmtUtils.mm
import org.xodium.vanillaplus.utils.TimeUtils.minutes
import org.xodium.vanillaplus.utils.TimeUtils.seconds
import org.xodium.vanillaplus.utils.TimeUtils.ticks
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * Represents a module that manages automatic server restarts at configured times.
 * The module enables scheduling and countdowns for server restarts.
 * It uses a boss bar to notify players of the remaining time until the restart.
 *
 * The auto-restart functionality will only be enabled if specified in the plugin's configuration.
 */
class AutoRestartModule : ModuleInterface {
    override fun enabled(): Boolean = Config.AutoRestartModule.ENABLED

    /**
     * Initializes the AutoRestartModule.
     */
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
                0.ticks,
                1.minutes
            )
        }
    }

    /**
     * Triggers a countdown for the server restart.
     */
    private fun countdown() {
        val totalMinutes = Config.AutoRestartModule.COUNTDOWN_START_MINUTES
        var remainingSeconds = totalMinutes * 60
        val totalSeconds = remainingSeconds
        val bossBar = bossbar(remainingSeconds)
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
            0.ticks,
            1.seconds
        )
    }

    /**
     * Returns true if the current time is equal to the time string in the plugin's configuration.
     *
     * @param restartTime the time to compare to the current time
     * @return true if the current time is equal to the restart time
     */
    private fun isTimeToStartCountdown(restartTime: LocalTime): Boolean {
        return ChronoUnit.MINUTES.between(
            LocalTime.now().truncatedTo(ChronoUnit.MINUTES),
            restartTime
        ) == Config.AutoRestartModule.COUNTDOWN_START_MINUTES.toLong()
    }

    /**
     * Returns a boss bar with the name and progress set in the plugin's configuration.
     *
     * @param timePlaceholder the time placeholder to replace in the boss bar name
     * @return a boss bar with the name and progress set in the plugin's configuration
     */
    private fun bossbar(timePlaceholder: Int): BossBar {
        return BossBar.bossBar(
            Config.AutoRestartModule.BOSSBAR_NAME.replace("%t", timePlaceholder.toString()).mm(),
            Config.AutoRestartModule.BOSSBAR_PROGRESS,
            Config.AutoRestartModule.BOSSBAR_COLOR,
            Config.AutoRestartModule.BOSSBAR_OVERLAY
        )
    }
}