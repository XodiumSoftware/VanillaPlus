@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus.mechanics.world

import org.bukkit.*
import org.bukkit.block.data.Lightable
import org.bukkit.block.data.type.Candle
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils
import org.xodium.illyriaplus.data.RitualData
import org.xodium.illyriaplus.interfaces.MechanicInterface
import org.xodium.illyriaplus.items.TeleportScrollItem
import org.xodium.illyriaplus.managers.RitualStorageManager

internal object TeleportMechanic : MechanicInterface {
    private const val CIRCLE_RADIUS = 3

    private val activeRituals = mutableMapOf<Location, RitualCircle>()
    private val ritualCenters = mutableMapOf<Location, Location>()

    override fun register(): Long {
        RitualStorageManager.load()
        return super.register()
    }

    /**
     * Represents a ritual circle for teleportation.
     *
     * @property center The center location of the ritual circle.
     * @property candles Set of candle locations belonging to this ritual.
     * @property isActive Whether the ritual is currently active (fire lit).
     * @property fireLocation The location of the fire block if active.
     */
    private data class RitualCircle(
        val center: Location,
        val candles: MutableSet<Location> = mutableSetOf(),
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

    /** Handles candle placement to detect ritual circles. */
    private fun blockPlace(event: BlockPlaceEvent) {
        val block = event.block

        if (!Tag.CANDLES.isTagged(block.type)) return

        findRitualCenter(block.location)?.let { center ->
            if (block.location.blockY != center.blockY) return@let
            if (!isValidCandlePosition(block.location, center)) return@let

            activeRituals.getOrPut(center) { RitualCircle(center) }.candles.add(block.location)
        }
    }

    /**
     * Checks if a candle is at a valid position relative to the ritual center.
     *
     * The ritual circle pattern consists of 16 candles arranged as follows:
     * ```
     * ..CCC..
     * .C...C.
     * C.....C
     * C..X..C    (X = center block, C = candle position)
     * C.....C
     * .C...C.
     * ..CCC..
     * ```
     *
     * Valid positions are the 16 'C' positions forming the circle pattern.
     */
    private fun isValidCandlePosition(
        candleLoc: Location,
        center: Location,
    ): Boolean {
        val dx = candleLoc.blockX - center.blockX
        val dz = candleLoc.blockZ - center.blockZ

        return when (dz) {
            -3 -> dx == -2 || dx == 0 || dx == 2
            -2 -> dx == -2 || dx == 2
            -1 -> dx == -3 || dx == 3
            0 -> dx == -3 || dx == 3
            1 -> dx == -3 || dx == 3
            2 -> dx == -2 || dx == 2
            3 -> dx == -2 || dx == 0 || dx == 2
            else -> false
        }
    }

    /** Handles candle breaks to update ritual circles. */
    private fun blockBreak(event: BlockBreakEvent) {
        val block = event.block

        if (!Tag.CANDLES.isTagged(block.type)) {
            if (block.type == Material.FIRE) {
                ritualCenters[block.location]?.let { location ->
                    activeRituals[location]?.let {
                        it.isActive = false
                        it.fireLocation = null
                    }
                    ritualCenters.remove(block.location)
                }
            }
            return
        }

        activeRituals.values.find { block.location in it.candles }?.let { circle ->
            circle.candles.remove(block.location)

            if (circle.candles.isEmpty()) {
                activeRituals.remove(circle.center)
                RitualStorageManager.removeRitual(circle.center)
            } else if (circle.isActive) {
                circle.fireLocation?.let { location ->
                    location.block.type = Material.AIR
                    circle.isActive = false
                    circle.fireLocation = null
                    ritualCenters.remove(location)
                }
            }
        }
    }

    /** Handles candle lighting with flint and steel or fire charge. */
    private fun playerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return

        val block = event.clickedBlock ?: return
        val player = event.player
        val item = event.item ?: return

        if (block.type == Material.FIRE) {
            ritualCenters[block.location]?.let { location ->
                activeRituals[location]?.let { checkForScrollInFire(it, player) }
            }
            return
        }
        if (!Tag.CANDLES.isTagged(block.type)) return
        if (!isIgnitionItem(item)) return

        val candleData = block.blockData as? Lightable ?: return

        if (candleData.isLit) return

        findRitualCenter(block.location)?.let { location -> activeRituals[location]?.let { checkAllCandlesLit(it) } }
    }

    /** Checks if all candles in a circle are lit and activates the ritual. */
    private fun checkAllCandlesLit(circle: RitualCircle) {
        if (circle.isActive) return

        val world = circle.center.world ?: return
        val candleConfigs = mutableMapOf<Location, Pair<Int, Material>>()
        val allLit =
            circle.candles.all { loc ->
                val block = loc.block

                if (!Tag.CANDLES.isTagged(block.type)) return@all false

                val candleData = block.blockData as? Candle ?: return@all false

                if (!candleData.isLit) return@all false

                candleConfigs[loc] = candleData.candles to block.type
                true
            }

        if (!allLit) return

        val ritualData = RitualData.fromLocation(circle.center, candleConfigs)

        RitualStorageManager.addRitual(ritualData)

        spawnParticleTrails(circle)

        val fireLoc = circle.center.clone().add(0.0, 1.0, 0.0)

        world.spawnEntity(fireLoc, EntityType.LIGHTNING_BOLT)
        fireLoc.block.type = Material.FIRE
        circle.isActive = true
        circle.fireLocation = fireLoc
        ritualCenters[fireLoc] = circle.center

        world
            .getNearbyEntities(fireLoc, 10.0, 10.0, 10.0)
            .filterIsInstance<Player>()
            .forEach { it.sendActionBar(Utils.MM.deserialize("<yellow>A teleportation portal opens...</yellow>")) }
    }

    /** Spawns particle trails from each candle to the center. */
    private fun spawnParticleTrails(circle: RitualCircle) {
        val center = circle.center.clone().add(0.0, 1.0, 0.0)

        circle.candles.forEach {
            Particle.TRAIL
                .builder()
                .location(center)
                .data(Particle.Trail(it.clone().add(0.0, 1.0, 0.0), Color.PURPLE, 20))
                .spawn()
        }
    }

    /** Checks for teleport scroll items in the fire and teleports the player. */
    private fun checkForScrollInFire(
        circle: RitualCircle,
        player: Player,
    ) {
        val fireLoc = circle.fireLocation ?: return
        val world = fireLoc.world ?: return

        if (!isPlayerInsideCircle(player, circle.center)) return

        world
            .getNearbyEntities(fireLoc, 1.5, 1.5, 1.5)
            .filterIsInstance<Item>()
            .filter { TeleportScrollItem.isTeleportScroll(it.itemStack) }
            .forEach {
                it.remove()
                performTeleport(player, circle)
            }
    }

    /**
     * Checks if the player is inside the ritual circle (not on candles).
     * The circle has radius 3, so inside means distance < 3 from center.
     */
    private fun isPlayerInsideCircle(
        player: Player,
        center: Location,
    ): Boolean {
        val dx = player.location.blockX - center.blockX
        val dz = player.location.blockZ - center.blockZ
        val distanceSquared = dx * dx + dz * dz

        return distanceSquared < CIRCLE_RADIUS * CIRCLE_RADIUS
    }

    /** Performs the teleportation. */
    private fun performTeleport(
        player: Player,
        circle: RitualCircle,
    ) {
        val candleConfigs =
            buildMap {
                circle.candles.forEach { loc ->
                    val block = loc.block
                    if (!Tag.CANDLES.isTagged(block.type)) return@forEach

                    val candleData = block.blockData as? Candle ?: return@forEach

                    put(loc, candleData.candles to block.type)
                }
            }

        val currentRitual = RitualData.fromLocation(circle.center, candleConfigs)
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
                player.sendActionBar(Utils.MM.deserialize("<green>You have been teleported!</green>"))
            }
        } else {
            player.sendActionBar(Utils.MM.deserialize("<red>No matching ritual found!</red>"))
        }

        circle.fireLocation?.let {
            it.block.type = Material.AIR
            ritualCenters.remove(it)
        }
        circle.isActive = false
        circle.fireLocation = null
        circle.candles.forEach {
            val block = it.block

            if (Tag.CANDLES.isTagged(block.type)) {
                val lightable = block.blockData as? Lightable ?: return@forEach

                lightable.isLit = false
                block.blockData = lightable
            }
        }
    }

    /** Prevents fire damage to players near ritual fires. */
    private fun entityDamage(event: EntityDamageEvent) {
        if (event.cause != EntityDamageEvent.DamageCause.FIRE &&
            event.cause != EntityDamageEvent.DamageCause.FIRE_TICK
        ) {
            return
        }

        val player = event.entity as? Player ?: return

        ritualCenters.keys.forEach {
            if (player.location.distanceSquared(it) < 4.0) {
                event.isCancelled = true
                return
            }
        }
    }

    /** Finds the center of a potential ritual circle for the given candle location. */
    private fun findRitualCenter(candleLoc: Location): Location? {
        val world = candleLoc.world ?: return null
        val cx = candleLoc.blockX
        val cy = candleLoc.blockY
        val cz = candleLoc.blockZ

        for (dx in -CIRCLE_RADIUS..CIRCLE_RADIUS) {
            for (dz in -CIRCLE_RADIUS..CIRCLE_RADIUS) {
                if (dx == 0 && dz == 0) continue

                val centerX = cx - dx
                val centerZ = cz - dz
                val centerLoc = Location(world, centerX.toDouble(), cy.toDouble(), centerZ.toDouble())

                if (isValidRitualCenter(centerLoc)) return centerLoc
            }
        }
        return null
    }

    /**
     * Checks if the given location is a valid center for a ritual circle.
     *
     * The ritual circle requires 16 candles arranged in this pattern:
     * ```
     * ..CCC..
     * .C...C.
     * C.....C
     * C..X..C    (X = center block, C = candle)
     * C.....C
     * .C...C.
     * ..CCC..
     * ```
     *
     * The center block can be any block type. All 16 candle positions must
     * have candles placed for the ritual to be considered valid.
     */
    private fun isValidRitualCenter(center: Location): Boolean {
        val world = center.world ?: return false
        val cx = center.blockX
        val cy = center.blockY
        val cz = center.blockZ
        val expectedPositions =
            listOf(
                // z = -3 row: 3 candles at x = -2, 0, +2
                Location(world, cx - 2.0, cy.toDouble(), cz - 3.0),
                Location(world, cx.toDouble(), cy.toDouble(), cz - 3.0),
                Location(world, cx + 2.0, cy.toDouble(), cz - 3.0),
                // z = -2 row: 2 candles at x = -2, +2
                Location(world, cx - 2.0, cy.toDouble(), cz - 2.0),
                Location(world, cx + 2.0, cy.toDouble(), cz - 2.0),
                // z = -1 row: 2 candles at x = -3, +3
                Location(world, cx - 3.0, cy.toDouble(), cz - 1.0),
                Location(world, cx + 3.0, cy.toDouble(), cz - 1.0),
                // z = 0 row: 2 candles at x = -3, +3
                Location(world, cx - 3.0, cy.toDouble(), cz.toDouble()),
                Location(world, cx + 3.0, cy.toDouble(), cz.toDouble()),
                // z = 1 row: 2 candles at x = -3, +3
                Location(world, cx - 3.0, cy.toDouble(), cz + 1.0),
                Location(world, cx + 3.0, cy.toDouble(), cz + 1.0),
                // z = 2 row: 2 candles at x = -2, +2
                Location(world, cx - 2.0, cy.toDouble(), cz + 2.0),
                Location(world, cx + 2.0, cy.toDouble(), cz + 2.0),
                // z = 3 row: 3 candles at x = -2, 0, +2
                Location(world, cx - 2.0, cy.toDouble(), cz + 3.0),
                Location(world, cx.toDouble(), cy.toDouble(), cz + 3.0),
                Location(world, cx + 2.0, cy.toDouble(), cz + 3.0),
            )

        return expectedPositions.all { Tag.CANDLES.isTagged(it.block.type) }
    }

    /** Checks if the item can light candles. */
    private fun isIgnitionItem(item: ItemStack): Boolean =
        item.type == Material.FLINT_AND_STEEL || item.type == Material.FIRE_CHARGE
}
