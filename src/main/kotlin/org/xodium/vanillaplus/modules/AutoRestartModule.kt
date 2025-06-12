/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.Utils
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/** Represents a module handling auto-restart mechanics within the system. */
class AutoRestartModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.autoRestartModule.enabled

    private val permPrefix: String = "${instance::class.simpleName}.autorestart".lowercase()

    @Suppress("UnstableApiUsage")
    override fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("autorestart")
                .requires { it.sender.hasPermission(perms()[0]) }
                .executes { it -> Utils.tryCatch(it) { countdown() } })
    }

    override fun perms(): List<Permission> {
        return listOf(
            Permission(
                "$permPrefix.use",
                "Allows use of the autorestart command",
                PermissionDefault.OP
            )
        )
    }

    init {
        if (enabled()) {
            instance.server.scheduler.runTaskTimerAsynchronously(
                instance,
                Runnable {
                    ConfigManager.data.autoRestartModule.restartTimes.forEach {
                        if (isTimeToStartCountdown(it)) {
                            instance.server.scheduler.runTask(instance, Runnable { countdown() })
                        }
                    }
                },
                ConfigManager.data.autoRestartModule.scheduleInitDelay,
                ConfigManager.data.autoRestartModule.scheduleInterval
            )
        }
    }

    /** Triggers a countdown for the server restart. */
    private fun countdown() {
        val totalMinutes = ConfigManager.data.autoRestartModule.countdownStartMinutes
        var remainingSeconds = totalMinutes * 60
        val totalSeconds = remainingSeconds
        val bossBar = ConfigManager.data.autoRestartModule.bossbar.toBossBar()
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
                        ConfigManager.data.autoRestartModule.bossbarName
                            .mm(Placeholder.component("time", displayTime.toString().mm()))
                    )
                    bossBar.progress(progress)
                    instance.server.onlinePlayers.forEach { player ->
                        if (!player.activeBossBars().contains(bossBar)) player.showBossBar(bossBar)
                    }
                } else {
                    instance.server.onlinePlayers.forEach { player -> player.hideBossBar(bossBar) }
                    instance.server.restart()
                }
            },
            ConfigManager.data.autoRestartModule.countdownInitDelay,
            ConfigManager.data.autoRestartModule.countdownInterval
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
        ) == ConfigManager.data.autoRestartModule.countdownStartMinutes.toLong()
    }
}