package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.bossbar.BossBar
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
import org.xodium.vanillaplus.managers.FormationManager
import org.xodium.vanillaplus.mobs.DarkKnight
import org.xodium.vanillaplus.mobs.Goblin
import org.xodium.vanillaplus.mobs.Orc
import org.xodium.vanillaplus.mobs.Troll
import org.xodium.vanillaplus.mobs.Warlord
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import org.xodium.vanillaplus.utils.Utils.broadcast
import org.xodium.vanillaplus.utils.Utils.dismiss
import kotlin.math.PI
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
        bossBars.remove(uuid)?.dismiss()
        formationTasks.remove(uuid)?.cancel()
    }

    @EventHandler
    fun on(event: PlayerJoinEvent) {
        bossBars.values.forEach { event.player.showBossBar(it) }
    }

    /**
     * Spawns a full horde formation centered at the given [location].
     *
     * Layout (front → rear along +Z):
     * ```
     *  G G G G G G G    Goblin      ×7   z=+0
     *  O O O O O O O    Orc         ×7   z=+8
     *  T O O O O O T    Orc+Troll   ×5+2 z=+16
     *      D W D        DarkKnight  ×2   z=+36 (flanking Warlord)
     * ```
     * Spacing between entities is controlled by [Config.formationSpacing].
     *
     * @param location The [Location] used as the front-center of the formation.
     */
    private fun spawnFormation(location: Location) {
        val warlordLoc = location.clone().add(0.0, 0.0, 36.0)
        val warlord = spawnWarlord(warlordLoc)
        val spacing = Config.formationSpacing
        val orcOuterX = 3 * spacing
        val rawFormation =
            listOf(
                spawnRow(location, 0.0, 7) { Goblin.spawn(it) },
                spawnRow(location, 8.0, 7) { Orc.spawn(it) },
                spawnRow(location, 16.0, 5) { Orc.spawn(it) },
                listOf(
                    Troll.spawn(location.clone().add(-orcOuterX, 0.0, 16.0)),
                    Troll.spawn(location.clone().add(orcOuterX, 0.0, 16.0)),
                ),
                listOf(
                    DarkKnight.spawn(warlordLoc.clone().add(-spacing, 0.0, 0.0)),
                    DarkKnight.spawn(warlordLoc.clone().add(spacing, 0.0, 0.0)),
                ),
            ).flatten().map {
                val raw = it.location.toVector().subtract(warlordLoc.toVector())
                it to Vector(raw.x, 0.0, raw.z)
            }
        val total = rawFormation.size
        val formation =
            rawFormation.mapIndexed { i, (mob, offset) -> FormationMemberData(mob, offset, 2 * PI * i / total) }

        startFormationTask(warlord, formation)
    }

    /**
     * Spawns a Warlord at the given [location] and registers its boss bar.
     * @param location The [Location] at which to spawn the Warlord.
     * @return The spawned [Zombie] entity.
     */
    private fun spawnWarlord(location: Location): Zombie =
        Warlord.spawn(location).also { warlord ->
            bossBars[warlord.uniqueId.toKotlinUuid()] = Warlord.bossBar.also { it.broadcast() }
        }

    /**
     * Spawns a centered row of entities along the X axis relative to [location] and returns them.
     * Entity spacing is controlled by [Config.formationSpacing].
     * @param location The origin of the formation.
     * @param rowZ The Z offset from the origin for this row.
     * @param count The number of entities to spawn.
     * @param spawner A lambda that spawns a single entity at the given [Location].
     */
    private fun spawnRow(
        location: Location,
        rowZ: Double,
        count: Int,
        spawner: (Location) -> Mob,
    ): List<Mob> {
        val spacing = Config.formationSpacing
        val halfWidth = (count - 1) * spacing / 2.0

        return (0 until count).map { i -> spawner(location.clone().add(-halfWidth + i * spacing, 0.0, rowZ)) }
    }

    /**
     * Starts a repeating task that delegates formation state each tick to [FormationManager].
     * @param warlord The commanding [Zombie] leading the formation.
     * @param formation The list of [FormationMemberData] belonging to this formation.
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
                    FormationManager.tick(warlord, formation)
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
        var formationSpacing: Double = 4.0
    }
}
