package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.entity.Villager
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
import org.xodium.vanillaplus.data.FormationData
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
    private val formationMembers = mutableMapOf<Uuid, MutableList<FormationMemberData>>()

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
        formationMembers.remove(uuid)?.let { dissolveFormation(it, event.entity.location) }
    }

    @EventHandler
    fun on(event: PlayerJoinEvent) {
        bossBars.values.forEach { event.player.showBossBar(it) }
    }

    /**
     * Spawns a full horde formation centered at the given [location].
     *
     * The layout is defined as a 2D character matrix in [Config.formation].
     * Each row maps to a Z offset (front → rear), each column to an X offset (centered).
     * Column and row spacing are controlled by [FormationData.formationSpacing] and [FormationData.rowSpacing].
     *
     * @param location The [Location] used as the front-center of the formation.
     */
    private fun spawnFormation(location: Location) {
        val formationData = Config.formation
        val layout = formationData.layout
        val spacing = formationData.formationSpacing
        val rowSpacing = formationData.rowSpacing
        val centerCol = (layout.maxOf { it.length } - 1) / 2.0
        val warlordRow = layout.indexOfFirst { 'W' in it }

        if (warlordRow == -1) {
            instance.logger.warning("Formation layout has no Warlord ('W') — horde aborted.")
            return
        }

        val warlordCol = layout[warlordRow].indexOf('W')
        val warlordLoc = location.clone().add((warlordCol - centerCol) * spacing, 0.0, warlordRow * rowSpacing)
        val warlord = spawnWarlord(warlordLoc)
        val rawFormation = mutableListOf<Pair<Mob, Vector>>()

        layout.forEachIndexed { rowIdx, row ->
            val rowZ = rowIdx * rowSpacing

            row.forEachIndexed { colIdx, char ->
                val x = (colIdx - centerCol) * spacing
                val mob: Mob =
                    when (char) {
                        'G' -> Goblin.spawn(location.clone().add(x, 0.0, rowZ))
                        'O' -> Orc.spawn(location.clone().add(x, 0.0, rowZ))
                        'T' -> Troll.spawn(location.clone().add(x, 0.0, rowZ))
                        'D' -> DarkKnight.spawn(location.clone().add(x, 0.0, rowZ))
                        else -> return@forEachIndexed
                    }
                val raw = mob.location.toVector().subtract(warlordLoc.toVector())

                rawFormation += mob to Vector(raw.x, 0.0, raw.z)
            }
        }

        val total = rawFormation.size
        val formation =
            rawFormation.mapIndexedTo(mutableListOf()) { i, (mob, offset) ->
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
            bossBars[warlord.uniqueId.toKotlinUuid()] = Warlord.bossBar.also { it.broadcast() }
        }

    /**
     * Starts a repeating task that delegates formation state each tick to [FormationManager].
     * @param warlord The commanding [Zombie] leading the formation.
     * @param formation The list of [FormationMemberData] belonging to this formation.
     */
    private fun startFormationTask(
        warlord: Zombie,
        formation: MutableList<FormationMemberData>,
    ) {
        val uuid = warlord.uniqueId.toKotlinUuid()

        formationMembers[uuid] = formation
        formationTasks[uuid] =
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

    /**
     * Converts all living [members] to villagers with a dark-spell-release effect.
     * Plays a curse-break burst at the [warlordLocation], then a liberation flash at each mob.
     * @param members The list of [FormationMemberData] to convert.
     * @param warlordLocation The [Location] of the dead warlord, used as the effect epicenter.
     */
    private fun dissolveFormation(
        members: MutableList<FormationMemberData>,
        warlordLocation: Location,
    ) {
        warlordLocation.world.apply {
            spawnParticle(Particle.WITCH, warlordLocation, 300, 2.0, 1.5, 2.0, 0.0)
            spawnParticle(Particle.PORTAL, warlordLocation, 200, 1.5, 1.0, 1.5, 0.5)
            spawnParticle(Particle.LARGE_SMOKE, warlordLocation, 60, 1.5, 1.0, 1.5, 0.0)
            playSound(warlordLocation, Sound.ENTITY_WITHER_DEATH, 1.0f, 0.6f)
            playSound(warlordLocation, Sound.ENTITY_ENDER_DRAGON_GROWL, 0.4f, 1.5f)
        }
        members.forEach { (mob, _, _) ->
            if (!mob.isDead && mob.isValid) {
                val loc = mob.location

                mob.world.apply {
                    spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 60, 0.4, 0.8, 0.4, 0.1)
                    spawnParticle(Particle.WITCH, loc, 20, 0.3, 0.6, 0.3, 0.0)
                    playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.4f, 1.8f)
                }
                mob.world.spawn(loc, Villager::class.java)
                mob.remove()
            }
        }
    }

    /** Represents the config of the module. */
    object Config {
        var detectionRange: Double = 48.0
        var idleCircleRadius: Double = 10.0
        var roamRadius: Double = 5.0
        var formation: FormationData =
            FormationData(
                listOf(
                    "GGGGGGG",
                    "OOOOOOO",
                    "TOOOOOT",
                    "..DWD..",
                ),
            )
    }
}
