@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus.mechanics.world

import org.bukkit.*
import org.bukkit.block.data.Lightable
import org.bukkit.block.data.type.Candle
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.Utils.BlockUtils.center
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.data.RitualData
import org.xodium.illyriaplus.interfaces.MechanicInterface
import org.xodium.illyriaplus.managers.RitualStorageManager

internal object TeleportMechanic : MechanicInterface {
    private val logger = instance.logger

    private val activeRituals = mutableMapOf<Location, RitualCircle>()

    override fun register(): Long {
        RitualStorageManager.load()
        return super.register()
    }

    /**
     * Represents a ritual circle for teleportation.
     *
     * @property center The center location of the ritual circle (skull location).
     * @property candles Map of candle locations to their configurations.
     * @property isActive Whether the ritual is currently active.
     * @property fireLocation The location of the fire block if active.
     */
    private data class RitualCircle(
        val center: Location,
        val candles: MutableMap<Location, Pair<Int, Material>> = mutableMapOf(),
        var isActive: Boolean = false,
        var fireLocation: Location? = null,
    )

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockPlaceEvent) = blockPlace(event)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockBreakEvent) = blockBreak(event)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) = playerInteract(event)

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: EntityDamageEvent) = entityDamage(event)

    /** Handles skull placement to activate ritual circles. */
    private fun blockPlace(event: BlockPlaceEvent) {
        val block = event.block

        if (block.type == Material.SKELETON_SKULL || block.type == Material.SKELETON_WALL_SKULL) {
            val center = block.location

            logger.info("[DEBUG] Skull placed at ${center.blockX}, ${center.blockY}, ${center.blockZ}")

            if (center.world?.environment != World.Environment.NORMAL) {
                logger.info("[DEBUG] Skull placement rejected: not in overworld")
                event.isCancelled = true
                event.player.sendActionBar(MM.deserialize("<red>Rituals can only be created in the overworld!</red>"))
                return
            }

            if (!isValidRitualPattern(center)) {
                logger.info("[DEBUG] Invalid ritual pattern at ${center.blockX}, ${center.blockY}, ${center.blockZ}")
                return
            }

            logger.info(
                "[DEBUG] Valid ritual pattern recognized at ${center.blockX}, ${center.blockY}, ${center.blockZ}",
            )

            val candleConfigs = getCandleConfigs(center)
            val circle = RitualCircle(center, candleConfigs.toMutableMap())

            activeRituals[center] = circle

            activateRitual(circle)
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

    /** Checks if a location is part of the ritual candle pattern. */
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
     * Checks if dx, dz form a valid candle position in the ritual pattern.
     *
     * Pattern uses radius 3 with mixed offsets:
     * - Corners at distance 3
     * - Top/bottom rows use tighter spacing (±1 instead of ±2)
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
     * Validates that all 16 candle positions have candles.
     *
     * The ritual circle pattern:
     * ```
     * ..CCC..
     * .C...C.
     * C.....C
     * C..X..C    (X = skull/center, C = candle)
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
        val grid = StringBuilder()

        grid.appendLine("[DEBUG] Pattern check at $cx, $cy, $cz (skull Y-level):")

        for (dz in -3..3) {
            val row = StringBuilder()
            for (dx in -3..3) {
                when {
                    dx == 0 && dz == 0 -> {
                        row.append("X")
                    }

                    isValidCandlePosition(dx, dz) -> {
                        val checkX = cx + dx
                        val checkZ = cz + dz
                        val loc = Location(world, checkX.toDouble(), cy.toDouble(), checkZ.toDouble())
                        val block = loc.block
                        val hasCandle = Tag.CANDLES.isTagged(block.type)
                        if (hasCandle) {
                            row.append("C")
                        } else {
                            row.append("?")
                            grid.appendLine("[DEBUG]   Missing candle at ($checkX, $cy, $checkZ): found ${block.type}")
                        }
                    }

                    else -> {
                        row.append(".")
                    }
                }
            }
            grid.appendLine("[DEBUG] $row")
        }
        logger.info(grid.toString().trimEnd())

        val expectedPositions = getRitualPositions(cx, cz)

        return expectedPositions.all { (x, z) ->
            val loc = Location(world, x.toDouble(), cy.toDouble(), z.toDouble())

            Tag.CANDLES.isTagged(loc.block.type)
        }
    }

    /**
     * Gets the 16 ritual candle positions relative to center.
     *
     * Matches the actual in-world structure (non-uniform spacing).
     */
    private fun getRitualPositions(
        cx: Int,
        cz: Int,
    ): List<Pair<Int, Int>> =
        listOf(
            // z = -3
            cx - 1 to cz - 3,
            cx to cz - 3,
            cx + 1 to cz - 3,
            // z = -2
            cx - 2 to cz - 2,
            cx + 2 to cz - 2,
            // z = -1
            cx - 3 to cz - 1,
            cx + 3 to cz - 1,
            // z = 0
            cx - 3 to cz,
            cx + 3 to cz,
            // z = 1
            cx - 3 to cz + 1,
            cx + 3 to cz + 1,
            // z = 2
            cx - 2 to cz + 2,
            cx + 2 to cz + 2,
            // z = 3
            cx - 1 to cz + 3,
            cx to cz + 3,
            cx + 1 to cz + 3,
        )

    /** Gets candle configurations from all ritual positions. */
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

    private fun activateRitual(circle: RitualCircle) {
        circle.isActive = true

        lightAllCandles(circle)
        spawnParticleTrails(circle)
    }

    /** Lights all candles in the ritual. */
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

    /** Extinguishes all candles in the ritual. */
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
     * Starts animated particle trails from candles to center.
     *
     * Trails start one-by-one, remain active, and are cancelled when ritual finishes.
     *
     * @param circle The ritual circle.
     */
    private fun spawnParticleTrails(circle: RitualCircle) {
        val plugin = instance
        val center =
            circle.center.block
                .center()
                .add(0.0, 1.0, 0.0)
        val candles = circle.candles.keys.toList()
        val activeTasks = mutableListOf<Int>()

        candles.forEachIndexed { index, loc ->
            val delay = index * 10L

            instance.server.scheduler.runTaskLater(
                plugin,
                Runnable {
                    val taskId =
                        instance.server.scheduler.scheduleSyncRepeatingTask(
                            plugin,
                            Runnable {
                                if (!circle.isActive) return@Runnable

                                val from = loc.clone().add(0.5, 1.0, 0.5)
                                val to = center.clone()
                                val direction = to.toVector().subtract(from.toVector())
                                val distance = direction.length()
                                val step = direction.normalize().multiply(0.3)
                                val current = from.clone()

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

                    activeTasks.add(taskId)

                    if (index == candles.lastIndex) triggerRitualFinish(circle, activeTasks)
                },
                delay,
            )
        }
    }

    /** Handles skull/candle/fire breaks to deactivate ritual circles. */
    private fun blockBreak(event: BlockBreakEvent) {
        val block = event.block
        val loc = block.location

        if (block.type == Material.SKELETON_SKULL || block.type == Material.SKELETON_WALL_SKULL) {
            activeRituals[loc]?.let { circle ->
                deactivateRitual(circle)
                activeRituals.remove(loc)
                RitualStorageManager.removeRitual(loc)
            }
            return
        }

        if (block.type == Material.FIRE) {
            activeRituals.values.find { it.fireLocation == loc }?.let { circle ->
                deactivateRitual(circle)
                activeRituals.remove(circle.center)
                RitualStorageManager.removeRitual(circle.center)
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

    /** Deactivates a ritual circle. */
    private fun deactivateRitual(circle: RitualCircle) {
        circle.fireLocation?.let { it.block.type = Material.AIR }
        circle.isActive = false
        extinguishAllCandles(circle)
        circle.center.world
            ?.getNearbyEntities(circle.center, 10.0, 10.0, 10.0)
            ?.filterIsInstance<Player>()
            ?.forEach { it.sendActionBar(MM.deserialize("<gray>The portal closes...</gray>")) }
    }

    /** Handles player interaction with the ritual fire. */
    private fun playerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val block = event.clickedBlock ?: return
        val player = event.player

        if (block.type == Material.FIRE) {
            activeRituals.values.find { it.fireLocation == block.location }?.let {
                if (!isPlayerInsideCircle(player, it.center)) {
                    player.sendActionBar(MM.deserialize("<red>Step inside the circle to use the portal!</red>"))
                    return
                }

                performTeleport(player, it)
                event.isCancelled = true
            }
        }
    }

    /**
     * Checks if the player is inside the ritual circle.
     * Inside means distance < 3 from center (not on candles).
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

    /** Performs the teleportation. */
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
                player.teleport(it.clone().add(0.5, 0.0, 0.5))
                player.sendActionBar(MM.deserialize("<green>You have been teleported!</green>"))
            }
        } else {
            player.sendActionBar(MM.deserialize("<red>No matching ritual found!</red>"))
        }

        deactivateRitual(circle)
        activeRituals.remove(circle.center)
        RitualStorageManager.removeRitual(circle.center)
    }

    /** Prevents fire damage to players near ritual fires. */
    private fun entityDamage(event: EntityDamageEvent) {
        if (event.cause != EntityDamageEvent.DamageCause.FIRE &&
            event.cause != EntityDamageEvent.DamageCause.FIRE_TICK
        ) {
            return
        }

        val player = event.entity as? Player ?: return

        activeRituals.values.forEach { circle ->
            circle.fireLocation?.let {
                if (player.location.distanceSquared(it) < 4.0) {
                    event.isCancelled = true
                    return
                }
            }
        }
    }

    /**
     * Handles final ritual sequence:
     * - Lightning strike
     * - Remove skull
     * - Place fire
     * - Trails continue briefly
     * - Then stop everything
     *
     * @param circle The ritual circle.
     * @param tasks Active particle task IDs.
     */
    private fun triggerRitualFinish(
        circle: RitualCircle,
        tasks: List<Int>,
    ) {
        val plugin = instance
        val world = circle.center.world ?: return

        instance.server.scheduler.runTaskLater(
            plugin,
            Runnable {
                val centerAbove = circle.center.clone()

                world.spawnEntity(centerAbove, EntityType.LIGHTNING_BOLT)

                instance.server.scheduler.runTaskLater(
                    plugin,
                    Runnable {
                        circle.center.block.type = Material.AIR
                        circle.center.block.type = Material.FIRE
                        circle.fireLocation = circle.center.clone()

                        instance.server.scheduler.runTaskLater(
                            plugin,
                            Runnable {
                                tasks.forEach { instance.server.scheduler.cancelTask(it) }
                            },
                            40L,
                        )
                    },
                    20L,
                )
            },
            40L,
        )
    }
}
