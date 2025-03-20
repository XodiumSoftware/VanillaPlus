/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.TimeUtils.minutes
import org.xodium.vanillaplus.utils.TimeUtils.ticks
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class AutoRestartModule : ModuleInterface {
    /**
     * Returns true if the module is enabled in the plugin's configuration.
     */
    override fun enabled(): Boolean = Config.AutoRestartModule.ENABLED

    init {
        if (enabled()) {
            instance.server.scheduler.runTaskTimer(
                instance,
                Runnable {
                    Config.AutoRestartModule.RESTART_TIMES.forEach { timeString ->
                        if (isTimeToStartCountdown(timeString)) {
                            var remainingMinutes = Config.AutoRestartModule.COUNTDOWN_START_MINUTES
                            instance.server.worlds.forEach { it.save() }
                            instance.server.scheduler.runTaskTimer(
                                instance,
                                Runnable {
                                    if (remainingMinutes > 0) {
                                        Audience.audience().showBossBar(bossbar())
                                        remainingMinutes--
                                    } else {
                                        Audience.audience().hideBossBar(bossbar())
                                        instance.server.restart()
                                    }
                                },
                                0.ticks,
                                1.minutes
                            )
                        }
                    }
                },
                0.ticks,
                1.minutes
            )
        }
    }

    /**
     * Returns true if the current time is equal to the time string in the plugin's configuration.
     */
    private fun isTimeToStartCountdown(timeString: String): Boolean {
        try {
            return ChronoUnit.MINUTES.between(
                LocalTime.now().truncatedTo(ChronoUnit.MINUTES),
                LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
            ) == Config.AutoRestartModule.COUNTDOWN_START_MINUTES.toLong()
        } catch (_: Exception) {
            instance.logger.warning("Invalid restart time format: $timeString")
            return false
        }
    }

    /**
     * Returns a boss bar with the name and progress set in the plugin's configuration.
     */
    private fun bossbar(): BossBar {
        return BossBar.bossBar(
            Config.AutoRestartModule.BOSSBAR_NAME.mm(),
            Config.AutoRestartModule.BOSSBAR_PROGRESS,
            Config.AutoRestartModule.BOSSBAR_COLOR,
            Config.AutoRestartModule.BOSSBAR_OVERLAY
        )
    }
}