@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.utils

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.scheduler.BukkitTask
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.pdcs.PlayerPDC.mana
import org.xodium.vanillaplus.utils.ManaUtils.NO_MANA_SOUND
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

    private val BAR_NAME = MM.deserialize("<gradient:#832466:#BF4299:#832466><b>Mana</b></gradient>")
    private val ACTIVE_GAME_MODES = setOf(GameMode.SURVIVAL, GameMode.ADVENTURE)
    private val bossBars = WeakHashMap<Player, BossBar>()
    private val hideTasks = WeakHashMap<Player, BukkitTask>()

    /**
     * Validates a blaze-rod left-click interaction and consumes mana if all checks pass.
     * Verifies the action is a left-click, the held item is a [Material.BLAZE_ROD] with [enchantment],
     * the player is in [GameMode.SURVIVAL] or [GameMode.ADVENTURE], and has at least [manaCost] mana.
     * On success, cancels the event, deducts [manaCost] mana, and updates the mana bar.
     * On insufficient mana, plays [NO_MANA_SOUND] and shows the bar without deducting.
     * @param event The [PlayerInteractEvent] to validate.
     * @param enchantment The [Enchantment] that must be present on the held item.
     * @param manaCost The amount of mana required and consumed on success.
     * @return The [Player] if all checks pass, `null` otherwise.
     */
    fun consumeMana(
        event: PlayerInteractEvent,
        enchantment: Enchantment,
        manaCost: Int,
    ): Player? {
        if (event.action != Action.LEFT_CLICK_AIR && event.action != Action.LEFT_CLICK_BLOCK) return null

        val item = event.item ?: return null

        if (item.type != Material.BLAZE_ROD) return null
        if (!item.containsEnchantment(enchantment)) return null

        val player = event.player

        if (player.gameMode !in ACTIVE_GAME_MODES) return null
        if (player.mana < manaCost) {
            player.playSound(NO_MANA_SOUND)
            showManaBar(player)
            return null
        }

        event.isCancelled = true
        player.mana -= manaCost
        showManaBar(player)

        return player
    }

    /**
     * Starts a repeating task that regenerates mana for all online players every
     * [Config.MANA_REGEN_PERIOD_TICKS] ticks, up to [Config.MAX_MANA].
     * Should be called once from [org.xodium.vanillaplus.VanillaPlus.onEnable].
     */
    fun startRegenTask() {
        ScheduleUtils.schedule(period = Config.MANA_REGEN_PERIOD_TICKS) {
            instance.server.onlinePlayers.forEach {
                if (it.mana < Config.MAX_MANA) {
                    it.mana = (it.mana + Config.MANA_REGEN_AMOUNT).coerceAtMost(Config.MAX_MANA)
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
        val bar =
            bossBars.getOrPut(player) {
                BossBar
                    .bossBar(BAR_NAME, progress, BossBar.Color.PURPLE, BossBar.Overlay.NOTCHED_10)
                    .also { player.showBossBar(it) }
            }

        bar.name(BAR_NAME)
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
