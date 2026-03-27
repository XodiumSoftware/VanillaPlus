package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.entity.Zombie
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.data.FormationMemberData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.mobs.DarkKnight
import org.xodium.vanillaplus.mobs.Goblin
import org.xodium.vanillaplus.mobs.Orc
import org.xodium.vanillaplus.mobs.Troll
import org.xodium.vanillaplus.mobs.Warlord
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlin.uuid.toKotlinUuid

/** Represents a module handling horde mechanics within the system. */
@OptIn(ExperimentalUuidApi::class)
internal object HordeModule : ModuleInterface {
    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("horde")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .playerExecuted { player, _ -> spawnFormation(player.location) },
                "Spawns a horde formation at your location",
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.horde".lowercase(),
                "Allows spawning a horde formation",
                PermissionDefault.OP,
            ),
        )

    private val bossBars = mutableMapOf<Uuid, BossBar>()
    private val formationTasks = mutableMapOf<Uuid, BukkitTask>()

    @EventHandler
    fun on(event: EntityDamageEvent) {
        val bossBar = bossBars[event.entity.uniqueId.toKotlinUuid()] ?: return
        val entity = event.entity as? LivingEntity ?: return
        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                val max = entity.getAttribute(Attribute.MAX_HEALTH)?.value ?: return@Runnable
                bossBar.progress((entity.health / max).coerceIn(0.0, 1.0).toFloat())
            },
            1L,
        )
    }

    @EventHandler
    fun on(event: EntityDeathEvent) {
        val uuid = event.entity.uniqueId.toKotlinUuid()
        bossBars.remove(uuid)?.let { bar -> instance.server.onlinePlayers.forEach { it.hideBossBar(bar) } }
        formationTasks.remove(uuid)?.cancel()
    }

    @EventHandler
    fun on(event: PlayerJoinEvent) {
        bossBars.values.forEach { event.player.showBossBar(it) }
    }

    /**
     * Spawns a full horde formation centered at the given [location].
     * The formation follows a medieval rows layout: goblins up front, orcs behind them,
     * trolls further back, dark knights flanking, and the warlord commanding from the rear.
     * @param location The [Location] used as the front-center of the formation.
     */
    private fun spawnFormation(location: Location) {
        val warlordLoc = location.clone().add(0.0, 0.0, 36.0)
        val warlord = spawnWarlord(warlordLoc)
        val rawFormation = mutableListOf<Pair<Mob, Vector>>()

        fun collectRow(entities: List<Mob>) =
            entities.forEach { mob ->
                val raw = mob.location.toVector().subtract(warlordLoc.toVector())
                rawFormation += mob to Vector(raw.x, 0.0, raw.z)
            }

        collectRow(spawnRow(location, 0.0, 4.0, 6) { Goblin.spawn(it) })
        collectRow(spawnRow(location, 8.0, 5.0, 4) { Orc.spawn(it) })
        collectRow(spawnRow(location, 18.0, 10.0, 2) { Troll.spawn(it) })
        collectRow(spawnRow(location, 28.0, 8.0, 2) { DarkKnight.spawn(it) })

        val total = rawFormation.size
        val formation =
            rawFormation.mapIndexed { i, (mob, offset) ->
                FormationMemberData(mob, offset, 2 * PI * i / total)
            }
        startFormationTask(warlord, formation)
    }

    /**
     * Spawns a Warlord at the given [location] and registers its boss bar.
     * @param location The [Location] at which to spawn the Warlord.
     * @return The spawned [Zombie] entity.
     */
    private fun spawnWarlord(location: Location): Zombie =
        Warlord.spawn(location).also { warlord ->
            val bossBar = Warlord.bossBar
            instance.server.onlinePlayers.forEach { it.showBossBar(bossBar) }
            bossBars[warlord.uniqueId.toKotlinUuid()] = bossBar
        }

    /**
     * Spawns a centered row of entities along the X axis relative to [location] and returns them.
     * @param location The origin of the formation.
     * @param rowZ The Z offset from the origin for this row.
     * @param spacing The distance between each entity in the row.
     * @param count The number of entities to spawn.
     * @param spawner A lambda that spawns a single entity at the given [Location].
     */
    private fun spawnRow(
        location: Location,
        rowZ: Double,
        spacing: Double,
        count: Int,
        spawner: (Location) -> Mob,
    ): List<Mob> {
        val halfWidth = (count - 1) * spacing / 2.0
        return (0 until count).map { i -> spawner(location.clone().add(-halfWidth + i * spacing, 0.0, rowZ)) }
    }

    /**
     * Starts a repeating task that drives two formation states:
     * - **Active** (a survival/adventure player is within [Config.detectionRange]): the [warlord] marches toward
     *   the nearest such player and all [formation] members hold their row offsets relative to the warlord.
     * - **Idle** (no player in range): the [warlord] stops moving and each [formation] member is free to
     *   roam within [Config.roamRadius] of its assigned circle slot; if it strays further it pathfinds back.
     */
    private fun startFormationTask(
        warlord: Zombie,
        formation: List<FormationMemberData>,
    ) {
        formationTasks[warlord.uniqueId.toKotlinUuid()] =
            instance.server.scheduler.runTaskTimer(
                instance,
                Runnable {
                    if (warlord.isDead || !warlord.isValid) return@Runnable
                    val target =
                        warlord.world.players
                            .filter { !it.isDead && it.gameMode in setOf(GameMode.SURVIVAL, GameMode.ADVENTURE) }
                            .filter {
                                it.location.distanceSquared(
                                    warlord.location,
                                ) <= Config.detectionRange * Config.detectionRange
                            }.minByOrNull { it.location.distanceSquared(warlord.location) }

                    if (target != null) {
                        warlord.pathfinder.moveTo(target, 1.0)
                        formation.forEach { (mob, marchOffset, _) ->
                            if (!mob.isDead && mob.isValid) {
                                mob.pathfinder.moveTo(
                                    warlord.location.clone().add(marchOffset),
                                    1.0,
                                )
                            }
                        }
                    } else {
                        warlord.pathfinder.stopPathfinding()
                        formation.forEach { (mob, _, idleAngle) ->
                            if (!mob.isDead && mob.isValid) {
                                val idleHome =
                                    warlord.location.clone().add(
                                        cos(idleAngle) * Config.idleCircleRadius,
                                        0.0,
                                        sin(idleAngle) * Config.idleCircleRadius,
                                    )
                                if (mob.location.distanceSquared(idleHome) > Config.roamRadius * Config.roamRadius) {
                                    mob.pathfinder.moveTo(idleHome, 1.0)
                                }
                            }
                        }
                    }
                },
                0L,
                10L,
            )
    }

    /** Represents the config of the module. */
    object Config {
        var detectionRange: Double = 48.0
        var idleCircleRadius: Double = 10.0
        var roamRadius: Double = 5.0
    }
}
