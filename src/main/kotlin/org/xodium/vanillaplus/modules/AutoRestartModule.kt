/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Bukkit
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.TimeUtils.minutes
import org.xodium.vanillaplus.utils.TimeUtils.seconds
import org.xodium.vanillaplus.utils.TimeUtils.ticks
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class AutoRestartModule : ModuleInterface {
    /**
     * Returns true if the module is enabled in the plugin's configuration.
     */
    override fun enabled(): Boolean = Config.AutoRestartModule.ENABLED

    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    init {
        if (enabled()) {
            instance.server.scheduler.runTaskTimer(
                instance,
                Runnable {
                    val now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES)
                    Config.AutoRestartModule.RESTART_TIMES.forEach { timeString ->
                        try {
                            val restartTime = LocalTime.parse(timeString, timeFormat)
                            val minutesUntilRestart = ChronoUnit.MINUTES.between(now, restartTime)
                            if (minutesUntilRestart == Config.AutoRestartModule.COUNTDOWN_START_MINUTES.toLong()) {
                                var remainingMinutes = Config.AutoRestartModule.COUNTDOWN_START_MINUTES
                                instance.server.scheduler.runTaskTimer(
                                    instance,
                                    Runnable {
                                        if (remainingMinutes > 0
                                            && Config.AutoRestartModule.COUNTDOWN_ANNOUNCE_AT.contains(remainingMinutes)
                                        ) {
                                            val message = Config.AutoRestartModule.MESSAGE_COUNTDOWN
                                                .replace("%time%", remainingMinutes.toString())
                                            Bukkit.broadcast(message.mm())
                                        } else if (remainingMinutes <= 0) {
                                            Bukkit.broadcast(Config.AutoRestartModule.MESSAGE_RESTARTING.mm())
                                            instance.server.scheduler.runTaskLater(
                                                instance,
                                                Runnable {
                                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart")
                                                },
                                                3.seconds
                                            )
                                        }
                                        remainingMinutes--
                                    },
                                    0.ticks,
                                    1.minutes
                                )
                            }
                        } catch (_: Exception) {
                            instance.logger.warning("Invalid restart time format: $timeString")
                        }
                    }
                },
                0.ticks,
                1.minutes
            )
        }
    }
}