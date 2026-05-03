@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus.mechanics.entity

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.audience.Audience
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.Utils.CommandUtils.playerExecuted
import org.xodium.illyriaplus.Utils.ScheduleUtils.schedule
import org.xodium.illyriaplus.bosses.AnubisBoss
import org.xodium.illyriaplus.data.CommandData
import org.xodium.illyriaplus.interfaces.BossInterface
import org.xodium.illyriaplus.interfaces.MechanicInterface
import kotlin.random.Random

/** Handles boss spawning and mechanics. */
internal object BossMechanic : MechanicInterface {
    /** List of all boss implementations. */
    private val bosses =
        listOf<BossInterface>(
            AnubisBoss,
        )

    /** Map of boss class names to boss implementations for quick lookup. */
    private val bossByName = bosses.associateBy { it.javaClass.simpleName }

    /** Map of active boss entities to their boss interface. */
    private val activeBosses = mutableMapOf<LivingEntity, BossInterface>()

    /** Random ability trigger chance (1 in 200 ticks = ~10 seconds on average). */
    private const val ABILITY_CHANCE = 0.005

    /** Distance at which players can see the boss bar. */
    private const val BOSS_BAR_RANGE = 32.0

    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("boss")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .then(
                        Commands
                            .argument("name", StringArgumentType.word())
                            .suggests { _, builder ->
                                bossByName.keys.forEach { builder.suggest(it) }
                                builder.buildFuture()
                            }.playerExecuted { player, ctx ->
                                bossByName[ctx.getArgument("name", String::class.java)]?.spawn(player.location)
                            },
                    ),
                "Spawns a boss at the player's location",
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.boss".lowercase(),
                "Allows use of the boss command",
                PermissionDefault.OP,
            ),
        )

    override fun register(): Long = super.register().apply { startScheduler() }

    @EventHandler
    fun on(event: EntityDeathEvent) {
        val boss = activeBosses[event.entity]

        if (boss != null) {
            boss.onDeath(event.entity)
            activeBosses.remove(event.entity)
        } else {
            bosses.forEach { it.onDeath(event.entity) }
        }
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        val entity = event.entity as? LivingEntity ?: return
        val boss = activeBosses[entity]

        if (boss != null) boss.onDamage(entity) else bosses.forEach { it.onDamage(entity) }
    }

    /**
     * Registers a boss entity as active.
     *
     * @param entity The boss entity.
     * @param boss The boss interface.
     */
    fun registerBoss(
        entity: LivingEntity,
        boss: BossInterface,
    ) {
        activeBosses[entity] = boss
    }

    /**
     * Starts the scheduler that handles boss abilities and boss bar visibility.
     */
    private fun startScheduler() {
        schedule(period = 1) {
            activeBosses.entries.removeAll { (entity, boss) ->
                val shouldRemove = !entity.isValid || entity.isDead

                if (!shouldRemove) {
                    if (Random.nextDouble() < ABILITY_CHANCE) boss.ability(entity)

                    updateBossBarViewers(entity, boss)
                } else {
                    boss.bossBar
                        .viewers()
                        .filterIsInstance<Audience>()
                        .forEach { boss.bossBar.removeViewer(it) }
                }

                shouldRemove
            }
        }
    }

    /**
     * Updates which players can see the boss bar based on distance.
     *
     * @param entity The boss entity.
     * @param boss The boss interface.
     */
    private fun updateBossBarViewers(
        entity: LivingEntity,
        boss: BossInterface,
    ) {
        val nearbyPlayers = entity.world.getNearbyPlayers(entity.location, BOSS_BAR_RANGE).map { it as Audience }
        val currentViewers =
            boss.bossBar
                .viewers()
                .filterIsInstance<Audience>()
                .toSet()

        nearbyPlayers.forEach { if (it !in currentViewers) boss.bossBar.addViewer(it) }
        currentViewers.forEach { if (it !in nearbyPlayers) boss.bossBar.removeViewer(it) }
    }
}
