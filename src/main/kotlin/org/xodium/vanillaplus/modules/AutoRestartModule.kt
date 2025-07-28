package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.BossBarData
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.TimeUtils
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** Represents a module handling auto-restart mechanics within the system. */
internal class AutoRestartModule : ModuleInterface<AutoRestartModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("autorestart")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx -> ctx.tryCatch { countdown() } },
                "Triggers a countdown for the server restart.",
                listOf("ar"),
            ),
        )

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance::class.simpleName}.autorestart".lowercase(),
                "Allows use of the autorestart command",
                PermissionDefault.OP,
            ),
        )

    init {
        if (enabled()) {
            instance.server.scheduler.runTaskTimerAsynchronously(
                instance,
                Runnable {
                    val zone = config.zoneId ?: ZoneId.systemDefault()
                    config.restartTimes.forEach {
                        if (isTimeToStartCountdown(zone, it)) {
                            instance.server.scheduler.runTask(instance, Runnable { countdown() })
                        }
                    }
                },
                config.scheduleInitDelay,
                config.scheduleInterval,
            )
        }
    }

    /** Triggers a countdown for the server restart. */
    private fun countdown() {
        val totalMinutes = config.countdownStartMinutes
        var remainingSeconds = totalMinutes * 60
        val totalSeconds = remainingSeconds
        val bossBar = config.bossbar.toBossBar()
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
                        config.bossbar.name
                            .mm(Placeholder.component("time", displayTime.toString().mm())),
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
            config.countdownInitDelay,
            config.countdownInterval,
        )
    }

    /**
     * Returns true if the current time is equal to the time string in the plugin's configuration.
     * @param zoneId the timezone to evaluate against.
     * @param restartTime the time to compare to the current time.
     * @return true if the current time is equal to the restart time.
     */
    private fun isTimeToStartCountdown(
        zoneId: ZoneId,
        restartTime: LocalTime,
    ): Boolean {
        val now = LocalTime.now(zoneId).truncatedTo(ChronoUnit.SECONDS)
        val trigger =
            restartTime
                .minusMinutes(config.countdownStartMinutes.toLong())
                .truncatedTo(ChronoUnit.SECONDS)
        return now.equals(trigger)
    }

    data class Config(
        override var enabled: Boolean = true,
        var zoneId: ZoneId? = null,
        var restartTimes: MutableList<LocalTime> =
            mutableListOf(
                LocalTime.MIDNIGHT,
                LocalTime.of(6, 0),
                LocalTime.NOON,
                LocalTime.of(18, 0),
            ),
        var bossbar: BossBarData =
            BossBarData(
                "⚡ RESTARTING in <time> minute(s) ⚡".fireFmt(),
                1.0f,
                BossBar.Color.RED,
                BossBar.Overlay.PROGRESS,
            ),
        var scheduleInitDelay: Long = TimeUtils.seconds(0),
        var scheduleInterval: Long = TimeUtils.seconds(1),
        var countdownInitDelay: Long = TimeUtils.seconds(0),
        var countdownInterval: Long = TimeUtils.seconds(1),
        var countdownStartMinutes: Int = 5,
    ) : ModuleInterface.Config
}
