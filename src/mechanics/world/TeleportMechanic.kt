@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus.mechanics.world

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.*
import org.bukkit.block.data.Lightable
import org.bukkit.block.data.type.Candle
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.Utils.BlockUtils.center
import org.xodium.illyriaplus.Utils.CommandUtils.playerExecuted
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.data.CommandData
import org.xodium.illyriaplus.data.RitualData
import org.xodium.illyriaplus.interfaces.MechanicInterface
import org.xodium.illyriaplus.managers.RitualStorageManager
import org.xodium.illyriaplus.mechanics.world.TeleportMechanic.activeRituals
import org.xodium.illyriaplus.mechanics.world.TeleportMechanic.closePortal
import org.xodium.illyriaplus.mechanics.world.TeleportMechanic.deactivateRitual
import org.xodium.illyriaplus.mechanics.world.TeleportMechanic.performTeleport
import org.xodium.illyriaplus.mechanics.world.TeleportMechanic.triggerRitualFinish
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Represents a mechanic handling teleportation within the system. */
internal object TeleportMechanic : MechanicInterface {
    private val activeRituals = mutableMapOf<Location, RitualCircle>()
    private val teleportCooldown = mutableMapOf<UUID, Long>()
    private val pendingTeleports = mutableSetOf<UUID>()

    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("teleportritual")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .then(
                        Commands
                            .literal("remove")
                            .then(
                                Commands
                                    .argument("ritual", StringArgumentType.greedyString())
                                    .suggests { _, builder ->
                                        RitualStorageManager
                                            .getAllRituals()
                                            .map { "${it.world},${it.x},${it.y},${it.z}" }
                                            .forEach { builder.suggest(it) }
                                        builder.buildFuture()
                                    }.playerExecuted { player, ctx ->
                                        val arg = StringArgumentType.getString(ctx, "ritual")
                                        val parts = arg.split(",")

                                        if (parts.size != 4) {
                                            player.sendActionBar(
                                                MM.deserialize("<red>Invalid format. Use world,x,y,z</red>"),
                                            )
                                            return@playerExecuted
                                        }

                                        val (worldName, xStr, yStr, zStr) = parts
                                        val world = instance.server.getWorld(worldName)

                                        if (world == null) {
                                            player.sendActionBar(MM.deserialize("<red>World not found.</red>"))
                                            return@playerExecuted
                                        }

                                        val center =
                                            Location(
                                                world,
                                                xStr.toDoubleOrNull() ?: 0.0,
                                                yStr.toDoubleOrNull() ?: 0.0,
                                                zStr.toDoubleOrNull() ?: 0.0,
                                            )
                                        val ritual =
                                            RitualStorageManager.getAllRituals().find {
                                                it.world == worldName &&
                                                    it.x == center.blockX &&
                                                    it.y == center.blockY &&
                                                    it.z == center.blockZ
                                            }

                                        if (ritual == null) {
                                            player.sendActionBar(MM.deserialize("<red>Ritual not found.</red>"))
                                            return@playerExecuted
                                        }

                                        RitualStorageManager.removeRitual(center)
                                        activeRituals[center]?.let {
                                            it.isActive = false
                                            it.activeTaskIds.forEach { id -> instance.server.scheduler.cancelTask(id) }
                                            it.activeTaskIds.clear()
                                            activeRituals.remove(center)
                                        }
                                        player.sendActionBar(MM.deserialize("<green>Ritual link removed.</green>"))
                                    },
                            ),
                    ),
                "Allows admins to remove ritual links from PDC",
                listOf("tr"),
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.teleportritual".lowercase(),
                "Allows use of the teleport ritual admin command",
                PermissionDefault.OP,
            ),
        )

    /**
     * Registers the mechanic and starts the background check task.
     *
     * On startup all rituals saved in [RitualStorageManager] are loaded into
     * [activeRituals] as inactive circles so destinations work without a skull.
     * Invalid or unlit rituals are removed from storage immediately.
     *
     * The repeating task (every 10 ticks) handles four things:
     * 1. Detects inactive rituals with invalid patterns → removes them.
     * 2. Detects inactive rituals whose candles are all lit → activates them.
     * 3. Detects active rituals with any unlit candle → deactivates them.
     * 4. Detects players inside active rituals that have a skull → triggers teleport.
     */
    override fun register(): Long =
        super.register().apply {
            RitualStorageManager.load()

            RitualStorageManager.getAllRituals().forEach { ritual ->
                val center = ritual.getCenter()
                if (!isValidRitualPattern(center)) {
                    RitualStorageManager.removeRitual(center)
                    return@forEach
                }
                val configs = getCandleConfigs(center)
                if (configs.size != 16) {
                    RitualStorageManager.removeRitual(center)
                    return@forEach
                }
                activeRituals[center] = RitualCircle(center, configs.toMutableMap())
            }

            instance.server.scheduler.scheduleSyncRepeatingTask(
                instance,
                {
                    activeRituals.values.filter { !it.isActive }.toList().forEach { circle ->
                        if (!isValidRitualPattern(circle.center) || !areAllCandlesLit(circle)) {
                            activeRituals.remove(circle.center)
                            RitualStorageManager.removeRitual(circle.center)
                        } else {
                            activateRitual(circle)
                        }
                    }
                    activeRituals.values.filter { it.isActive }.forEach {
                        if (!areAllCandlesLit(it)) {
                            deactivateRitual(it)
                            activeRituals.remove(it.center)
                            RitualStorageManager.removeRitual(it.center)
                        }
                    }
                    val now = System.currentTimeMillis()

                    activeRituals.values
                        .filter { it.isActive && isSkullPresent(it.center) }
                        .toList()
                        .forEach { circle ->
                            findPlayerInsideCircle(circle)?.let {
                                if (teleportCooldown.getOrDefault(it.uniqueId, 0) > now) return@let
                                startTeleportSequence(it, circle)
                                teleportCooldown[it.uniqueId] = now + 5000
                            }
                        }
                },
                0L,
                10L,
            )
        }

    /**
     * Internal state of a ritual circle.
     *
     * A circle begins inactive when a skull is placed. It becomes active once
     * all 16 candles are lit, spawning particle trails and enabling teleport.
     * After a teleport, the circle is closed but the candles remain lit.
     *
     * @property center The skull location at the center of the candle pattern.
     * @property candles Map of each candle [Location] to its (count, [Material]) pair.
     *   This configuration is used to match against other rituals in storage.
     * @property isActive Whether trails are running and teleport is enabled.
     * @property activeTaskIds Bukkit scheduler task IDs for the repeating particle
     *   trail tasks. Cancelled on deactivation or close.
     */
    private data class RitualCircle(
        val center: Location,
        val candles: MutableMap<Location, Pair<Int, Material>> = mutableMapOf(),
        var isActive: Boolean = false,
        val activeTaskIds: MutableList<Int> = mutableListOf(),
    )

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockPlaceEvent) = blockPlace(event)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockBreakEvent) = blockBreak(event)

    /**
     * Handles skull placement and prevents candle modification on active rituals.
     *
     * **Skull branch:**
     * - Only allowed in the overworld ([World.Environment.NORMAL]).
     * - Validates the surrounding 16-candle pattern.
     * - Stores the ritual as inactive; player must light candles next.
     *
     * **Candle branch:**
     * - Prevents placing candles on positions that belong to an active ritual.
     *   This stops players from altering a lit pattern while the portal is open.
     */
    private fun blockPlace(event: BlockPlaceEvent) {
        val block = event.block

        if (block.type == Material.SKELETON_SKULL || block.type == Material.SKELETON_WALL_SKULL) {
            val center = block.location

            if (center.world?.environment != World.Environment.NORMAL) {
                event.isCancelled = true
                event.player.sendActionBar(MM.deserialize("<red>Rituals can only be created in the overworld!</red>"))
                return
            }

            if (!isValidRitualPattern(center)) return

            val candleConfigs = getCandleConfigs(center)

            activeRituals[center]?.let { existing ->
                existing.candles.clear()
                existing.candles.putAll(candleConfigs)
                return
            }

            val circle = RitualCircle(center, candleConfigs.toMutableMap())
            activeRituals[center] = circle
            return
        }

        if (Tag.CANDLES.isTagged(block.type)) {
            activeRituals.values.find { it.isActive }?.let { circle ->
                if (isPartOfRitual(block.location, circle.center)) {
                    event.isCancelled = true
                    event.player.sendActionBar(
                        MM.deserialize("<red>Cannot modify candles while ritual is active!</red>"),
                    )
                }
            }
        }
    }

    /**
     * Checks whether [loc] is one of the 16 candle positions surrounding [center].
     *
     * Only compares X and Z at the same Y level.
     */
    private fun isPartOfRitual(
        loc: Location,
        center: Location,
    ): Boolean {
        if (loc.blockY != center.blockY) return false

        val dx = loc.blockX - center.blockX
        val dz = loc.blockZ - center.blockZ

        return isValidCandlePosition(dx, dz)
    }

    /**
     * Checks if relative offsets [dx], [dz] match the candle pattern.
     *
     * The 16 candles are arranged in a non-uniform diamond with a bounding box
     * of 7x7 blocks.  The offsets are:
     *
     * ```
     *      z=-3   z=-2   z=-1   z=0    z=1    z=2    z=3
     * x=-3              C             C             C
     * x=-2       C                                C
     * x=-1  C      C      C      C      C      C      C
     * x=0   C             X             X             C
     * x=1   C      C      C      C      C      C      C
     * x=2        C                                C
     * x=3              C             C             C
     * ```
     *
     * `X` = skull/center.  `C` = candle.
     */
    private fun isValidCandlePosition(
        dx: Int,
        dz: Int,
    ): Boolean =
        when (dz) {
            -3, 3 -> dx == -1 || dx == 0 || dx == 1
            -2, 2 -> dx == -2 || dx == 2
            -1, 0, 1 -> dx == -3 || dx == 3
            else -> false
        }

    /**
     * Validates that every expected candle position actually contains a candle block.
     *
     * The ASCII pattern below shows the 7×7 footprint from above.
     *
     * ```
     * ..CCC..
     * .C...C.
     * C.....C
     * C..X..C    (X = skull, C = candle)
     * C.....C
     * .C...C.
     * ..CCC..
     * ```
     */
    private fun isValidRitualPattern(center: Location): Boolean {
        val world = center.world ?: return false
        val cx = center.blockX
        val cy = center.blockY
        val cz = center.blockZ

        return getRitualPositions(cx, cz).all { (x, z) ->
            Tag.CANDLES.isTagged(Location(world, x.toDouble(), cy.toDouble(), z.toDouble()).block.type)
        }
    }

    /**
     * Returns the 16 absolute candle positions surrounding [cx], [cz].
     *
     * The offsets are fixed; changing them breaks existing saved rituals.
     */
    private fun getRitualPositions(
        cx: Int,
        cz: Int,
    ): List<Pair<Int, Int>> =
        listOf(
            // z = -3 (top edge, 3 candles)
            cx - 1 to cz - 3,
            cx to cz - 3,
            cx + 1 to cz - 3,
            // z = -2 (2 candles)
            cx - 2 to cz - 2,
            cx + 2 to cz - 2,
            // z = -1 (2 candles)
            cx - 3 to cz - 1,
            cx + 3 to cz - 1,
            // z = 0 (2 candles)
            cx - 3 to cz,
            cx + 3 to cz,
            // z = 1 (2 candles)
            cx - 3 to cz + 1,
            cx + 3 to cz + 1,
            // z = 2 (2 candles)
            cx - 2 to cz + 2,
            cx + 2 to cz + 2,
            // z = 3 (bottom edge, 3 candles)
            cx - 1 to cz + 3,
            cx to cz + 3,
            cx + 1 to cz + 3,
        )

    /**
     * Reads the candle configuration from the 16 ritual positions around [center].
     *
     * Each entry maps the candle [Location] to a pair of (candle count, [Material]).
     * This map is later used by [RitualData.fromLocation] for storage and matching.
     */
    private fun getCandleConfigs(center: Location): Map<Location, Pair<Int, Material>> {
        val world = center.world ?: return emptyMap()
        val cx = center.blockX
        val cy = center.blockY
        val cz = center.blockZ
        val configs = mutableMapOf<Location, Pair<Int, Material>>()
        val expectedPositions = getRitualPositions(cx, cz)

        expectedPositions.forEach { (x, z) ->
            val loc = Location(world, x.toDouble(), cy.toDouble(), z.toDouble())
            val block = loc.block

            if (Tag.CANDLES.isTagged(block.type)) {
                val candleData = block.blockData as? Candle
                if (candleData != null) configs[loc] = candleData.candles to block.type
            }
        }

        return configs
    }

    /**
     * Activates a ritual once all candles are lit.
     *
     * Steps:
     * 1. Forces every candle to lit state (safety in case some were unlit briefly).
     * 2. Starts one-by-one particle trails from each candle to the center.
     * 3. Saves the candle configuration to storage so other circles can match it.
     */
    private fun activateRitual(circle: RitualCircle) {
        circle.isActive = true

        lightAllCandles(circle)
        spawnParticleTrails(circle)
        RitualStorageManager.addRitual(RitualData.fromLocation(circle.center, circle.candles))
    }

    /** Returns `true` if the block at [center] is a skeleton skull. */
    private fun isSkullPresent(center: Location): Boolean =
        center.block.type == Material.SKELETON_SKULL || center.block.type == Material.SKELETON_WALL_SKULL

    /** Returns `true` if every candle in [circle] is currently lit. */
    private fun areAllCandlesLit(circle: RitualCircle): Boolean =
        circle.candles.keys.all {
            val lightable = it.block.blockData as? Lightable

            lightable?.isLit == true
        }

    /** Forces all candles in [circle] to lit state, updating block data. */
    private fun lightAllCandles(circle: RitualCircle) {
        circle.candles.keys.forEach {
            if (!Tag.CANDLES.isTagged(it.block.type)) return@forEach

            val lightable = it.block.blockData as? Lightable ?: return@forEach

            if (!lightable.isLit) {
                lightable.isLit = true
                it.block.blockData = lightable
            }
        }
    }

    /** Forces all candles in [circle] to unlit state, updating block data. */
    private fun extinguishAllCandles(circle: RitualCircle) {
        circle.candles.keys.forEach {
            if (!Tag.CANDLES.isTagged(it.block.type)) return@forEach

            val lightable = it.block.blockData as? Lightable ?: return@forEach

            if (lightable.isLit) {
                lightable.isLit = false
                it.block.blockData = lightable
            }
        }
    }

    /**
     * Spawns animated [Particle.CRIT] trails from each candle toward the center.
     *
     * Each candle starts its trail with a staggered delay (index × 10 ticks).
     * While the ritual is active, every 5 ticks a trail particle moves from the
     * candle to the center in 0.3-block steps, accompanied by an amethyst chime.
     *
     * The last candle to start its trail schedules the finish sequence via
     * [triggerRitualFinish].
     */
    private fun spawnParticleTrails(circle: RitualCircle) {
        val center = circle.center.block.center()
        val candles = circle.candles.keys.toList()

        candles.forEachIndexed { index, loc ->
            val delay = index * 10L

            instance.server.scheduler.runTaskLater(
                instance,
                Runnable {
                    val taskId =
                        instance.server.scheduler.scheduleSyncRepeatingTask(
                            instance,
                            Runnable {
                                if (!circle.isActive) return@Runnable

                                val from = loc.block.center()
                                val to = center.clone()
                                val direction = to.toVector().subtract(from.toVector())
                                val distance = direction.length()
                                val step = direction.normalize().multiply(0.3)
                                val current = from.clone()

                                from.world.playSound(
                                    from,
                                    Sound.BLOCK_AMETHYST_BLOCK_CHIME,
                                    SoundCategory.AMBIENT,
                                    0.5f,
                                    1.5f,
                                )

                                var traveled = 0.0

                                while (traveled < distance) {
                                    current.add(step)
                                    current.world.spawnParticle(
                                        Particle.CRIT,
                                        current,
                                        1,
                                        0.0,
                                        0.0,
                                        0.0,
                                        0.0,
                                    )
                                    traveled += step.length()
                                }
                            },
                            0L,
                            5L,
                        )

                    circle.activeTaskIds.add(taskId)

                    if (index == candles.lastIndex) triggerRitualFinish(circle)
                },
                delay,
            )
        }
    }

    /**
     * Finishes the activation sequence with a lightning strike followed by cloud rings.
     *
     * Scheduled 40 ticks after the last candle trail begins.
     * 1. Lightning strikes the skull (no fire since it hits the block).
     * 2. Particle trail tasks are cancelled.
     * 3. 20 ticks later, expanding cloud rings appear to signal readiness.
     */
    private fun triggerRitualFinish(circle: RitualCircle) {
        val world = circle.center.world ?: return
        val center = circle.center.clone().add(0.5, 0.5, 0.5)

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                world.spawnEntity(circle.center.clone(), EntityType.LIGHTNING_BOLT)
                circle.activeTaskIds.forEach { instance.server.scheduler.cancelTask(it) }
                circle.activeTaskIds.clear()

                instance.server.scheduler.runTaskLater(
                    instance,
                    Runnable {
                        repeat(2) { ring ->
                            val radius = 2.0 + ring
                            val count = (radius * 12).toInt()

                            for (i in 0 until count) {
                                val angle = 2 * PI * i / count
                                val x = center.x + radius * cos(angle)
                                val z = center.z + radius * sin(angle)
                                val loc = Location(world, x, center.y, z)

                                world.spawnParticle(Particle.CLOUD, loc, 1, 0.05, 0.0, 0.05, 0.01)
                                world.spawnParticle(
                                    Particle.CRIT,
                                    loc.clone().add(0.0, 0.2, 0.0),
                                    1,
                                    0.05,
                                    0.05,
                                    0.05,
                                    0.0,
                                )
                            }
                        }

                        spawnPurpleCandleTrail(circle)
                    },
                    20L,
                )
            },
            40L,
        )
    }

    /** Returns the first player standing inside [circle], or `null` if none. */
    private fun findPlayerInsideCircle(circle: RitualCircle): Player? =
        circle.center.world
            ?.getNearbyEntities(circle.center, 3.0, 3.0, 3.0)
            ?.filterIsInstance<Player>()
            ?.find { isPlayerInsideCircle(it, circle.center) }

    /**
     * Checks whether [player] is within 3 blocks (horizontal) of [center].
     *
     * Measured in block coordinates; distance² < 9 means strictly inside
     * the circle, not standing on the outer candle ring.
     */
    private fun isPlayerInsideCircle(
        player: Player,
        center: Location,
    ): Boolean {
        val dx = player.location.blockX - center.blockX
        val dz = player.location.blockZ - center.blockZ
        val distanceSquared = dx * dx + dz * dz

        return distanceSquared < 9
    }

    /**
     * Starts a 60-tick charge-up sequence before teleporting [player].
     *
     * While charging, a rotating spiral of [Particle.END_ROD] and [Particle.PORTAL]
     * particles spawn around the player. A 3..2..1.. countdown is shown in the action bar.
     * At "1" a beacon beam spawns at the player's location. After the delay,
     * [performTeleport] executes and a matching beam spawns at the destination.
     *
     * If the player leaves the circle, the portal closes, or the player disconnects,
     * the teleport is cancelled.
     */
    private fun startTeleportSequence(
        player: Player,
        circle: RitualCircle,
    ) {
        if (player.uniqueId in pendingTeleports) return
        pendingTeleports.add(player.uniqueId)

        val chargeTaskId =
            instance.server.scheduler.scheduleSyncRepeatingTask(
                instance,
                { spawnChargeUpParticles(player) },
                0L,
                2L,
            )

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                player.sendActionBar(MM.deserialize("<aqua>3..</aqua>"))
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1.0f, 0.8f)
            },
            0L,
        )

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                player.sendActionBar(MM.deserialize("<aqua>2..</aqua>"))
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1.0f, 1.0f)
            },
            20L,
        )

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                player.sendActionBar(MM.deserialize("<aqua>1..</aqua>"))
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1.0f, 1.2f)
                spawnBeaconBeam(player.location)
            },
            40L,
        )

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                instance.server.scheduler.cancelTask(chargeTaskId)
                pendingTeleports.remove(player.uniqueId)

                if (!player.isOnline) return@Runnable
                if (!circle.isActive) {
                    player.sendActionBar(MM.deserialize("<red>The portal closed before you could teleport!</red>"))
                    return@Runnable
                }
                if (!isPlayerInsideCircle(player, circle.center)) {
                    player.sendActionBar(MM.deserialize("<red>Teleport cancelled - you left the circle!</red>"))
                    return@Runnable
                }

                performTeleport(player, circle)
            },
            60L,
        )
    }

    /**
     * Spawns rotating spiral particles around [player] during the charge-up.
     *
     * Every call places 6 [Particle.END_ROD] particles in a short vertical spiral
     * and 3 [Particle.PORTAL] particles at the player's feet.
     */
    private fun spawnChargeUpParticles(player: Player) {
        val center = player.location.clone().add(0.0, 0.5, 0.0)
        val world = player.world
        val time = System.currentTimeMillis() / 150.0

        for (i in 0 until 6) {
            val angle = 2 * PI * i / 6 + time
            val radius = 0.6
            val x = center.x + radius * cos(angle)
            val z = center.z + radius * sin(angle)
            val y = center.y + (i % 3) * 0.4
            world.spawnParticle(Particle.END_ROD, x, y, z, 1, 0.0, 0.0, 0.0, 0.0)
        }

        world.spawnParticle(Particle.PORTAL, center.x, center.y - 0.5, center.z, 3, 0.3, 0.1, 0.3, 0.3)
    }

    /** Draws a purple [Particle.WITCH] line from [from] to [to] in 0.3-block steps. */
    private fun drawPurpleLine(
        from: Location,
        to: Location,
    ) {
        val direction = to.toVector().subtract(from.toVector())
        val distance = direction.length()
        val step = direction.normalize().multiply(0.3)
        val current = from.clone()
        var traveled = 0.0

        while (traveled < distance) {
            current.add(step)
            current.world.spawnParticle(Particle.WITCH, current, 1, 0.0, 0.0, 0.0, 0.0)
            traveled += step.length()
        }
    }

    /**
     * Spawns a repeating purple particle trail connecting all candles in a loop.
     *
     * The candles are sorted by angle around the center so the trail follows
     * the diamond perimeter. Started after the cloud effect in [triggerRitualFinish]
     * and cancelled when the ritual closes or deactivates.
     */
    private fun spawnPurpleCandleTrail(circle: RitualCircle) {
        val candles = circle.candles.keys.toList()
        if (candles.size < 2) return

        val center = circle.center
        val sorted =
            candles.sortedBy {
                val dx = it.blockX - center.blockX
                val dz = it.blockZ - center.blockZ
                kotlin.math.atan2(dz.toDouble(), dx.toDouble())
            }

        val taskId =
            instance.server.scheduler.scheduleSyncRepeatingTask(
                instance,
                Runnable {
                    if (!circle.isActive) return@Runnable

                    for (i in sorted.indices) {
                        val from = sorted[i].block.center()
                        val to = sorted[(i + 1) % sorted.size].block.center()
                        drawPurpleLine(from, to)
                    }
                },
                0L,
                5L,
            )

        circle.activeTaskIds.add(taskId)
    }

    /**
     * Spawns a tall vertical beacon beam at [location].
     *
     * Uses [Particle.END_ROD] in a column from 2 blocks below up to 30 blocks above.
     */
    private fun spawnBeaconBeam(location: Location) {
        val world = location.world ?: return
        val base = location.clone()

        for (y in -2..30) {
            val loc = base.clone().add(0.0, y.toDouble(), 0.0)
            world.spawnParticle(Particle.END_ROD, loc, 1, 0.0, 0.0, 0.0, 0.0)
        }
        world.playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0f, 1.0f)
    }

    /**
     * Handles breaking skulls or candles.
     *
     * - Skull break → full [deactivateRitual] (extinguishes candles, cancels tasks).
     * - Candle break → full [deactivateRitual] for the affected circle.
     */
    private fun blockBreak(event: BlockBreakEvent) {
        val block = event.block
        val loc = block.location

        if (block.type == Material.SKELETON_SKULL || block.type == Material.SKELETON_WALL_SKULL) {
            activeRituals[loc]?.let { circle ->
                circle.isActive = false
                circle.activeTaskIds.forEach { instance.server.scheduler.cancelTask(it) }
                circle.activeTaskIds.clear()
                activeRituals.remove(loc)
                RitualStorageManager.removeRitual(loc)
            }
            return
        }

        if (Tag.CANDLES.isTagged(block.type)) {
            activeRituals.values.find { loc in it.candles }?.let { circle ->
                deactivateRitual(circle)
                activeRituals.remove(circle.center)
                RitualStorageManager.removeRitual(circle.center)
            }
        }
    }

    /**
     * Fully deactivates a ritual circle.
     *
     * Cancels all particle tasks, extinguishes every candle, and notifies
     * nearby players. Used when a skull or candle is broken, or when a candle
     * is extinguished while the portal is active.
     *
     * This is **not** called after a normal teleport; [closePortal] is used
     * instead so candles stay lit.
     */
    private fun deactivateRitual(circle: RitualCircle) {
        circle.isActive = false
        circle.activeTaskIds.forEach { instance.server.scheduler.cancelTask(it) }
        circle.activeTaskIds.clear()
        extinguishAllCandles(circle)
        circle.center.world
            ?.getNearbyEntities(circle.center, 10.0, 10.0, 10.0)
            ?.filterIsInstance<Player>()
            ?.forEach { it.sendActionBar(MM.deserialize("<gray>The portal closes...</gray>")) }
    }

    /**
     * Teleports [player] to the matching ritual if one exists.
     *
     * Matching criteria:
     * - The target ritual must have the **exact same** candle configuration
     *   (material and candle count for every position).
     * - The target must be a **different** location (not the same circle).
     *
     * After teleport, the source portal is closed via [closePortal].
     */
    private fun performTeleport(
        player: Player,
        circle: RitualCircle,
    ) {
        val currentRitual = RitualData.fromLocation(circle.center, circle.candles)
        val targetRitual =
            RitualStorageManager
                .findMatchingRitual(currentRitual.candles)
                ?.takeIf {
                    it.world != circle.center.world?.name ||
                        it.x != circle.center.blockX ||
                        it.y != circle.center.blockY ||
                        it.z != circle.center.blockZ
                }

        if (targetRitual != null) {
            targetRitual.getCenter().let {
                spawnBeaconBeam(it.clone().add(0.5, 0.0, 0.5))
                player.teleport(it.clone().add(0.5, 0.0, 0.5))
                player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f)
                player.sendActionBar(MM.deserialize("<green>You have been teleported!</green>"))
            }
        } else {
            player.sendActionBar(MM.deserialize("<red>No matching ritual found!</red>"))
        }

        closePortal(circle)
    }

    /**
     * Removes the source portal from tracking and consumes the skull.
     *
     * Called after a successful (or failed) teleport. The skull block is replaced
     * with [Material.AIR] and the candle circle remains intact so it can be
     * re-activated later by placing another skull.
     *
     * Contrasts with [deactivateRitual], which fully shuts everything down.
     */
    private fun closePortal(circle: RitualCircle) {
        circle.activeTaskIds.forEach { instance.server.scheduler.cancelTask(it) }
        circle.activeTaskIds.clear()
        circle.center.block.type = Material.AIR
        activeRituals.remove(circle.center)
        RitualStorageManager.removeRitual(circle.center)
    }
}
