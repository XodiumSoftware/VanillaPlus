@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus.mechanics.entity

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.audience.Audience
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.Utils.CommandUtils.playerExecuted
import org.xodium.illyriaplus.Utils.ScheduleUtils.schedule
import org.xodium.illyriaplus.bosses.AnubisBoss
import org.xodium.illyriaplus.data.CommandData
import org.xodium.illyriaplus.interfaces.BossInterface
import org.xodium.illyriaplus.interfaces.MechanicInterface
import org.xodium.illyriaplus.pdcs.WorldPDC.addAnchoredBoss
import org.xodium.illyriaplus.pdcs.WorldPDC.anchoredBosses
import org.xodium.illyriaplus.pdcs.WorldPDC.removeAnchoredBoss
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

    /** Map of active boss entities to their anchor location. */
    private val bossAnchors = mutableMapOf<LivingEntity, Location>()

    /** Set of chunk keys that have spawned anchored bosses (world:chunkX:chunkZ). */
    private val anchoredBossChunks = mutableSetOf<String>()

    /** Maximum distance a boss can wander from its anchor before being pulled back. */
    private const val ANCHOR_LEASH_RANGE = 48.0

    /** Distance at which the boss starts walking back to anchor. */
    private const val ANCHOR_RETURN_THRESHOLD = 32.0

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
                            .literal("spawn")
                            .then(
                                Commands
                                    .argument("name", StringArgumentType.word())
                                    .suggests { _, builder ->
                                        bossByName.keys.forEach { builder.suggest(it) }
                                        builder.buildFuture()
                                    }.playerExecuted { player, ctx ->
                                        val bossName = ctx.getArgument("name", String::class.java)
                                        val boss = bossByName[bossName]
                                        if (boss != null) {
                                            val spawnLoc = player.location.clone()
                                            val entity = boss.spawn(spawnLoc)

                                            player.world.addAnchoredBoss(bossName, spawnLoc)
                                            player.sendMessage("Anchored $bossName at your location!")

                                            val chunk = spawnLoc.chunk

                                            anchoredBossChunks.add(getChunkKey(player.world.name, chunk.x, chunk.z))
                                            bossAnchors[entity] = spawnLoc
                                        } else {
                                            player.sendMessage("Unknown boss: $bossName")
                                        }
                                    },
                            ),
                    ).then(
                        Commands
                            .literal("remove")
                            .then(
                                Commands
                                    .argument("boss", StringArgumentType.greedyString())
                                    .suggests { ctx, builder ->
                                        val sender = ctx.source.sender
                                        val world =
                                            if (sender is Player) sender.world else instance.server.worlds.firstOrNull()

                                        world?.anchoredBosses?.forEach { builder.suggest(it.toString()) }

                                        builder.buildFuture()
                                    }.executes { ctx ->
                                        val sender = ctx.source.sender
                                        val world =
                                            if (sender is Player) sender.world else instance.server.worlds.firstOrNull()

                                        if (world == null) {
                                            sender.sendMessage("No world available")
                                            return@executes 0
                                        }

                                        val bossString = ctx.getArgument("boss", String::class.java)
                                        val anchored = world.anchoredBosses
                                        val index = anchored.indexOfFirst { it.toString() == bossString }

                                        if (index == -1) {
                                            sender.sendMessage("Boss not found: $bossString")
                                            return@executes 0
                                        }

                                        val removed = anchored[index]

                                        world.removeAnchoredBoss(index)
                                        sender.sendMessage("Removed anchored boss: $removed")
                                        1
                                    },
                            ),
                    ),
                "Manages anchored bosses: /boss spawn <name>, /boss remove [index]",
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
            bossAnchors.remove(event.entity)
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

    @EventHandler
    fun on(event: ChunkLoadEvent) {
        val chunk = event.chunk
        val world = chunk.world
        val chunkKey = getChunkKey(world.name, chunk.x, chunk.z)
        val anchored = world.anchoredBosses
        val bossesInChunk = anchored.filter { it.getLocation()?.chunk == chunk }

        if (bossesInChunk.isNotEmpty()) {
            anchoredBossChunks.add(chunkKey)
            bossesInChunk.forEach {
                val boss = bossByName[it.bossClassName]
                val location = it.getLocation()

                if (boss != null && location != null) {
                    val entity = boss.spawn(location)

                    bossAnchors[entity] = location.clone()
                }
            }
        }
    }

    @EventHandler
    fun on(event: ChunkUnloadEvent) {
        val chunk = event.chunk
        val chunkKey = getChunkKey(chunk.world.name, chunk.x, chunk.z)

        if (chunkKey in anchoredBossChunks) {
            activeBosses.entries.removeAll { (entity, _) ->
                val entityChunk = entity.location.chunk
                val shouldRemove = entityChunk == chunk

                if (shouldRemove) {
                    entity.remove()
                    bossAnchors.remove(entity)
                }

                shouldRemove
            }
            anchoredBossChunks.remove(chunkKey)
        }
    }

    /**
     * Generates a unique key for a chunk.
     */
    private fun getChunkKey(
        worldName: String,
        chunkX: Int,
        chunkZ: Int,
    ): String = "$worldName:$chunkX:$chunkZ"

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
     * Starts the scheduler that handles boss abilities, boss bar visibility, and anchor leash.
     */
    private fun startScheduler() {
        schedule(period = 1) {
            activeBosses.entries.removeAll { (entity, boss) ->
                val shouldRemove = !entity.isValid || entity.isDead

                if (!shouldRemove) {
                    if (Random.nextDouble() < ABILITY_CHANCE) boss.ability(entity)

                    updateBossBarViewers(entity, boss)
                    checkAnchorLeash(entity)
                } else {
                    boss.bossBar
                        .viewers()
                        .filterIsInstance<Audience>()
                        .forEach { boss.bossBar.removeViewer(it) }
                    bossAnchors.remove(entity)
                }

                shouldRemove
            }
        }
    }

    /**
     * Checks if the boss has wandered too far from its anchor and walks it back if necessary.
     *
     * @param entity The boss entity.
     */
    private fun checkAnchorLeash(entity: LivingEntity) {
        val anchor = bossAnchors[entity] ?: return

        if (entity.world != anchor.world) return

        val distance = entity.location.distance(anchor)

        if (distance > ANCHOR_LEASH_RANGE) {
            entity.teleport(anchor)
        } else if (distance > ANCHOR_RETURN_THRESHOLD) {
            val direction = anchor.toVector().subtract(entity.location.toVector()).normalize()

            entity.velocity = direction.multiply(0.3)
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
