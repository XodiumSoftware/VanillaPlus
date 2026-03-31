package org.xodium.vanillaplus.utils

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.pdcs.PlayerPDC.mana
import org.xodium.vanillaplus.utils.Utils.MM
import java.util.*

/** Utility object for managing the shared mana pool: bossbar display and passive regeneration. */
internal object ManaUtils {
    object Config {
        const val MAX_MANA = 100
        const val BAR_HIDE_DELAY_TICKS = 60L
        const val MANA_REGEN_AMOUNT = 5
        const val MANA_REGEN_PERIOD_TICKS = 40L
    }

    /** Sound played when an action is blocked due to insufficient mana. */
    val NO_MANA_SOUND: Sound = Sound.sound(Key.key("block.beacon.deactivate"), Sound.Source.PLAYER, 1.0f, 1.0f)

    private val bossBars = WeakHashMap<Player, BossBar>()
    private val hideTasks = WeakHashMap<Player, BukkitTask>()

    /**
     * Starts a repeating task that regenerates mana for all online players every
     * [Config.MANA_REGEN_PERIOD_TICKS] ticks, up to [Config.MAX_MANA].
     * Should be called once from [org.xodium.vanillaplus.VanillaPlus.onEnable].
     */
    fun startRegenTask() {
        ScheduleUtils.schedule(period = Config.MANA_REGEN_PERIOD_TICKS) {
            instance.server.onlinePlayers.forEach { player ->
                if (player.mana < Config.MAX_MANA) {
                    player.mana = (player.mana + Config.MANA_REGEN_AMOUNT).coerceAtMost(Config.MAX_MANA)
                }
            }
        }
    }

    /**
     * Shows or updates the mana [BossBar] for [player], then schedules it to be hidden after
     * [Config.BAR_HIDE_DELAY_TICKS] ticks. Any pending hide task is cancelled and rescheduled on
     * each call so the bar stays visible while the player is actively casting.
     * @param player The [Player] whose mana bar should be shown.
     */
    fun showManaBar(player: Player) {
        val progress = (player.mana.toFloat() / Config.MAX_MANA).coerceIn(0f, 1f)
        val name = MM.deserialize("<gradient:#832466:#BF4299:#832466><b>Mana</b></gradient>")
        val bar =
            bossBars.getOrPut(player) {
                BossBar
                    .bossBar(name, progress, BossBar.Color.PURPLE, BossBar.Overlay.NOTCHED_10)
                    .also { player.showBossBar(it) }
            }
        bar.name(name)
        bar.progress(progress)

        hideTasks.remove(player)?.cancel()
        hideTasks[player] =
            instance.server.scheduler.runTaskLater(
                instance,
                Runnable {
                    player.hideBossBar(bar)
                    bossBars.remove(player)
                },
                Config.BAR_HIDE_DELAY_TICKS,
            )
    }
}
