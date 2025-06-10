/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ModuleManager
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.Utils
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/** Represents a module handling auto-restart mechanics within the system. */
class AutoRestartModule : ModuleInterface {
    override fun enabled(): Boolean = Config.data.autoRestartModule.enabled

    @Suppress("UnstableApiUsage")
    override fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("autorestart")
                .requires { it.sender.hasPermission(Perms.AutoRestart.USE) }
                .executes { it -> Utils.tryCatch(it) { countdown() } })
    }

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
                Config.AutoRestartModule.SCHEDULE_INIT_DELAY,
                Config.AutoRestartModule.SCHEDULE_INTERVAL
            )
        }
    }

    /** Triggers a countdown for the server restart. */
    @OptIn(DelicateCoroutinesApi::class)
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
                    val progress = remainingSeconds.toFloat() / totalSeconds
                    bossBar.name(
                        Config.AutoRestartModule.BOSSBAR_NAME.fireFmt()
                            .mm(Placeholder.component("time", displayTime.toString().mm()))
                    )
                    bossBar.progress(progress)
                    instance.server.onlinePlayers.forEach { player ->
                        if (!player.activeBossBars().contains(bossBar)) player.showBossBar(bossBar)
                    }
                } else {
                    instance.server.onlinePlayers.forEach { player -> player.hideBossBar(bossBar) }
                    GlobalScope.launch {
                        ModuleManager.discordModule.sendEventEmbed(
                            title = "🔄 Server Restart",
                            description = "**The server is restarting now!**\nPlease rejoin in a moment.",
                            color = 0xFF8800
                        )
                    }
                    instance.server.restart()
                }
            },
            Config.AutoRestartModule.COUNTDOWN_INIT_DELAY,
            Config.AutoRestartModule.COUNTDOWN_INTERVAL
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