@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.enchantments

import io.papermc.paper.registry.data.EnchantmentRegistryEntry
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.entity.SmallFireball
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.EnchantmentInterface
import org.xodium.vanillaplus.pdcs.PlayerPDC.mana
import org.xodium.vanillaplus.utils.ScheduleUtils
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.displayName
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.uuid.ExperimentalUuidApi

/** Represents an object handling inferno enchantment implementation within the system. */
@OptIn(ExperimentalUuidApi::class)
@Suppress("UnstableApiUsage")
internal object InfernoEnchantment : EnchantmentInterface<PlayerInteractEvent> {
    object Config {
        const val MAX_MANA = 100
        const val MANA_COST_PER_FIREBALL = 10
        const val BAR_HIDE_DELAY_TICKS = 60L
        const val MANA_REGEN_AMOUNT = 5
        const val MANA_REGEN_PERIOD_TICKS = 40L
        val LAUNCH_SOUND: Sound = Sound.sound(Key.key("entity.blaze.shoot"), Sound.Source.HOSTILE, 1.0f, 1.0f)
        val NO_MANA_SOUND: Sound = Sound.sound(Key.key("block.fire.extinguish"), Sound.Source.PLAYER, 1.0f, 0.8f)
    }

    private val bossBars = WeakHashMap<Player, BossBar>()
    private val hideTasks = WeakHashMap<Player, BukkitTask>()

    override fun invoke(builder: EnchantmentRegistryEntry.Builder): EnchantmentRegistryEntry.Builder =
        builder
            .description(key.displayName())
            .anvilCost(4)
            .maxLevel(1)
            .weight(1)
            .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(20, 5))
            .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(65, 5))
            .activeSlots(EquipmentSlotGroup.MAINHAND)

    override fun effect(event: PlayerInteractEvent) {
        if (event.action != Action.LEFT_CLICK_AIR && event.action != Action.LEFT_CLICK_BLOCK) return

        val item = event.item ?: return

        if (item.type != Material.BLAZE_ROD) return
        if (!item.containsEnchantment(get())) return

        val player = event.player

        if (player.gameMode != GameMode.SURVIVAL) return

        val angleOffsets = doubleArrayOf(0.0)
        val manaCost = angleOffsets.size * Config.MANA_COST_PER_FIREBALL

        if (player.mana < manaCost) {
            player.playSound(Config.NO_MANA_SOUND)
            showManaBar(player)
            return
        }

        event.isCancelled = true
        player.mana -= manaCost
        showManaBar(player)

        val direction = player.location.direction.normalize()
        val spawnLocation = player.eyeLocation.add(direction.clone().multiply(1.5))

        for (angleDeg in angleOffsets) {
            val radians = angleDeg * PI / 180.0
            val cos = cos(radians)
            val sin = sin(radians)
            val spread =
                Vector(
                    direction.x * cos - direction.z * sin,
                    direction.y,
                    direction.x * sin + direction.z * cos,
                ).normalize()
            val fireball = player.world.spawn(spawnLocation, SmallFireball::class.java)

            fireball.shooter = player
            fireball.direction = spread.multiply(1.5)
            fireball.yield = 0.0f
            spawnFireballTrail(fireball)
        }

        player.playSound(Config.LAUNCH_SOUND)
    }

    /**
     * Spawns a repeating particle trail behind [fireball] every tick until the entity is no longer valid.
     * Emits [Particle.FLAME] and [Particle.LAVA] at the fireball's current location.
     * @param fireball The [SmallFireball] to trail.
     */
    private fun spawnFireballTrail(fireball: SmallFireball) {
        var task: BukkitTask? = null
        task =
            instance.server.scheduler.runTaskTimer(
                instance,
                Runnable {
                    if (!fireball.isValid) {
                        task?.cancel()
                        return@Runnable
                    }
                    val loc = fireball.location

                    Particle.FLAME
                        .builder()
                        .location(loc)
                        .count(5)
                        .offset(0.05, 0.05, 0.05)
                        .spawn()
                    Particle.LAVA
                        .builder()
                        .location(loc)
                        .count(1)
                        .spawn()
                },
                1L,
                1L,
            )
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
    private fun showManaBar(player: Player) {
        val progress = (player.mana.toFloat() / Config.MAX_MANA).coerceIn(0f, 1f)
        val name = MM.deserialize("<gradient:#CB2D3E:#EF473A><b>Mana</b></gradient>")
        val bar =
            bossBars.getOrPut(player) {
                BossBar
                    .bossBar(name, progress, BossBar.Color.RED, BossBar.Overlay.NOTCHED_10)
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
