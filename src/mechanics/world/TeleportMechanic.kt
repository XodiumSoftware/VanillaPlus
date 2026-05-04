@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus.mechanics.world

import org.bukkit.*
import org.bukkit.block.data.Lightable
import org.bukkit.block.data.type.Candle
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.Utils.BlockUtils.center
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.data.RitualData
import org.xodium.illyriaplus.interfaces.MechanicInterface
import org.xodium.illyriaplus.managers.RitualStorageManager

internal object TeleportMechanic : MechanicInterface {
    private val activeRituals = mutableMapOf<Location, RitualCircle>()

    override fun register(): Long =
        super.register().apply {
            RitualStorageManager.load()
            instance.server.scheduler.scheduleSyncRepeatingTask(
                instance,
                {
                    activeRituals.values.filter { !it.isActive }.forEach {
                        if (areAllCandlesLit(it)) activateRitual(it)
                    }
                    activeRituals.values.filter { it.isActive }.forEach {
                        if (!areAllCandlesLit(it)) {
                            deactivateRitual(it)
                            activeRituals.remove(it.center)
                            RitualStorageManager.removeRitual(it.center)
                        }
                    }
                    activeRituals.values.filter { it.isActive }.toList().forEach { circle ->
                        findPlayerInsideCircle(circle)?.let {
                            spawnTeleportBeam(it)
                            performTeleport(it, circle)
                        }
                    }
                },
                0L,
                10L,
            )
        }

    /**
     * Represents a ritual circle for teleportation.
     *
     * @property center The center location of the ritual circle (skull location).
     * @property candles Map of candle locations to their configurations.
     * @property isActive Whether the ritual is currently active.
     * @property activeTaskIds Scheduler task IDs for particle trails.
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

    /** Handles skull placement to activate ritual circles. */
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

        return getRitualPositions(cx, cz).all { (x, z) ->
            Tag.CANDLES.isTagged(Location(world, x.toDouble(), cy.toDouble(), z.toDouble()).block.type)
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
        RitualStorageManager.addRitual(RitualData.fromLocation(circle.center, circle.candles))
    }

    /** Checks if all candles in the ritual are lit. */
    private fun areAllCandlesLit(circle: RitualCircle): Boolean =
        circle.candles.keys.all {
            val lightable = it.block.blockData as? Lightable

            lightable?.isLit == true
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
     * Handles final ritual sequence: lightning strike at center and consumes the skull.
     *
     * @param circle The ritual circle.
     */
    private fun triggerRitualFinish(circle: RitualCircle) {
        val world = circle.center.world ?: return

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                world.spawnEntity(circle.center.clone(), EntityType.LIGHTNING_BOLT)
                circle.center.block.type = Material.AIR
            },
            40L,
        )
    }

    /** Finds the first player inside the ritual circle. */
    private fun findPlayerInsideCircle(circle: RitualCircle): Player? =
        circle.center.world
            ?.getNearbyEntities(circle.center, 3.0, 3.0, 3.0)
            ?.filterIsInstance<Player>()
            ?.find { isPlayerInsideCircle(it, circle.center) }

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

    /** Spawns a vertical beam on the player before teleport. */
    private fun spawnTeleportBeam(player: Player) {
        val loc = player.location.clone().add(0.0, 0.5, 0.0)

        repeat(20) {
            loc.add(0.0, 0.3, 0.0)
            loc.world.spawnParticle(Particle.END_ROD, loc, 1, 0.0, 0.0, 0.0, 0.0)
        }
        player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f)
    }

    /** Handles skull/candle breaks to deactivate ritual circles. */
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
        circle.isActive = false
        circle.activeTaskIds.forEach { instance.server.scheduler.cancelTask(it) }
        circle.activeTaskIds.clear()
        extinguishAllCandles(circle)
        circle.center.world
            ?.getNearbyEntities(circle.center, 10.0, 10.0, 10.0)
            ?.filterIsInstance<Player>()
            ?.forEach { it.sendActionBar(MM.deserialize("<gray>The portal closes...</gray>")) }
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
}
