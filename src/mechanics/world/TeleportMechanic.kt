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
import org.xodium.illyriaplus.data.RitualLocation
import org.xodium.illyriaplus.interfaces.MechanicInterface
import org.xodium.illyriaplus.managers.RitualStorageManager
import java.util.*
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Handles teleportation rituals using candle patterns and skulls. */
internal object TeleportMechanic : MechanicInterface {
    private val activeRituals = mutableMapOf<Location, RitualCircle>()
    private val teleportCooldown = mutableMapOf<UUID, Long>()
    private val pendingTeleports = mutableSetOf<UUID>()

    /** Messages used by this mechanic. */
    object Messages {
        const val INVALID_FORMAT = "<red>Invalid format. Use world,x,y,z</red>"
        const val WORLD_NOT_FOUND = "<red>World not found.</red>"
        const val RITUAL_NOT_FOUND = "<red>Ritual not found.</red>"
        const val RITUAL_PAIR_REMOVED = "<green>Ritual pair removed.</green>"
        const val RITUAL_OVERWORLD_ONLY = "<red>Rituals can only be created in the overworld!</red>"
        const val CANDLE_MODIFY_ACTIVE = "<red>Cannot modify candles while ritual is active!</red>"
        const val TELEPORT_COUNTDOWN_3 = "<aqua>3..</aqua>"
        const val TELEPORT_COUNTDOWN_2 = "<aqua>2..</aqua>"
        const val TELEPORT_COUNTDOWN_1 = "<aqua>1..</aqua>"
        const val PORTAL_CLOSED = "<red>The portal closed before you could teleport!</red>"
        const val TELEPORT_CANCELLED_LEFT_CIRCLE = "<red>Teleport cancelled - you left the circle!</red>"
        const val PORTAL_CLOSES = "<gray>The portal closes...</gray>"
        const val TELEPORT_SUCCESS = "<green>You have been teleported!</green>"
        const val NO_PAIRED_RITUAL = "<red>No paired ritual found!</red>"
        const val RITUAL_LINKED = "<green>Ritual linked! The portal is now active.</green>"
        const val RITUAL_WAITING =
            "<yellow>Ritual created. Place another with the same candle pattern to link.</yellow>"
    }

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
                                            .getAllRitualLocations()
                                            .map { "${it.world},${it.x},${it.y},${it.z}" }
                                            .forEach { builder.suggest(it) }
                                        builder.buildFuture()
                                    }.playerExecuted { player, ctx ->
                                        val arg = StringArgumentType.getString(ctx, "ritual")
                                        val parts = arg.split(",")

                                        if (parts.size != 4) {
                                            player.sendActionBar(MM.deserialize(Messages.INVALID_FORMAT))
                                            return@playerExecuted
                                        }

                                        val (worldName, xStr, yStr, zStr) = parts
                                        val world = instance.server.getWorld(worldName)

                                        if (world == null) {
                                            player.sendActionBar(MM.deserialize(Messages.WORLD_NOT_FOUND))
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
                                            RitualStorageManager.getAllRitualLocations().find {
                                                it.world == worldName &&
                                                    it.x == center.blockX &&
                                                    it.y == center.blockY &&
                                                    it.z == center.blockZ
                                            }

                                        if (ritual == null) {
                                            player.sendActionBar(MM.deserialize(Messages.RITUAL_NOT_FOUND))
                                            return@playerExecuted
                                        }

                                        RitualStorageManager.removePair(center)
                                        activeRituals[center]?.let {
                                            deactivateRitual(it)
                                            activeRituals.remove(center)
                                        }
                                        player.sendActionBar(MM.deserialize(Messages.RITUAL_PAIR_REMOVED))
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
     * @return The time taken to register the mechanic in milliseconds.
     */
    override fun register(): Long =
        super.register().apply {
            RitualStorageManager.load()
            RitualStorageManager.getAllRitualLocations().forEach {
                val center = it.getCenter()

                if (!isValidRitualPattern(center)) {
                    RitualStorageManager.removePair(center)
                    return@forEach
                }

                val configs = getCandleConfigs(center)

                if (configs.size != 16) {
                    RitualStorageManager.removePair(center)
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
                            RitualStorageManager.removePair(circle.center)
                        } else {
                            activateRitual(circle)
                        }
                    }
                    activeRituals.values.filter { it.isActive }.forEach {
                        if (!areAllCandlesLit(it)) {
                            deactivateRitual(it)
                            activeRituals.remove(it.center)
                            RitualStorageManager.removePair(it.center)
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
     * @property center The skull location at the center of the candle pattern.
     * @property candles Map of each candle [Location] to its (count, [Material]) pair.
     * @property isActive Whether trails are running and teleport is enabled.
     * @property activeTaskIds Bukkit scheduler task IDs for particle trail tasks.
     */
    private data class RitualCircle(
        val center: Location,
        val candles: MutableMap<Pair<Int, Int>, Pair<Int, Material>> = mutableMapOf(),
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
     * @param event The BlockPlaceEvent to handle.
     */
    private fun blockPlace(event: BlockPlaceEvent) {
        val block = event.block

        if (block.type == Material.SKELETON_SKULL || block.type == Material.SKELETON_WALL_SKULL) {
            val center = block.location

            if (center.world?.environment != World.Environment.NORMAL) {
                event.isCancelled = true
                event.player.sendActionBar(MM.deserialize(Messages.RITUAL_OVERWORLD_ONLY))
                return
            }

            if (!isValidRitualPattern(center)) return

            val candleConfigs = getCandleConfigs(center)
            val ritualLocation = RitualLocation.fromLocation(center, toAbsoluteMap(center, candleConfigs))

            if (RitualStorageManager.isInPair(ritualLocation)) {
                activeRituals[center]?.let { existing ->
                    existing.candles.clear()
                    existing.candles.putAll(candleConfigs)
                } ?: run {
                    activeRituals[center] = RitualCircle(center, candleConfigs.toMutableMap())
                }
                return
            }

            val pair = RitualStorageManager.tryCreatePair(ritualLocation)

            if (pair != null) {
                activeRituals[center] = RitualCircle(center, candleConfigs.toMutableMap())

                val otherCenter =
                    pair.source.getCenter().takeIf {
                        it.x != center.x || it.y != center.y || it.z != center.z
                    } ?: pair.destination.getCenter()

                activeRituals[otherCenter]?.let { activateRitual(it) }
                activeRituals[center]?.let { activateRitual(it) }
                event.player.sendActionBar(MM.deserialize(Messages.RITUAL_LINKED))
            } else {
                activeRituals[center] = RitualCircle(center, candleConfigs.toMutableMap())
                event.player.sendActionBar(MM.deserialize(Messages.RITUAL_WAITING))
            }
            return
        }

        if (Tag.CANDLES.isTagged(block.type)) {
            activeRituals.values.find { it.isActive }?.let { circle ->
                if (isPartOfRitual(block.location, circle.center)) {
                    event.isCancelled = true
                    event.player.sendActionBar(MM.deserialize(Messages.CANDLE_MODIFY_ACTIVE))
                }
            }
        }
    }

    /**
     * Checks whether a location is one of the 16 candle positions surrounding the center.
     *
     * @param loc The location to check.
     * @param center The center of the ritual circle.
     * @return True if the location is part of the ritual pattern.
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
     * Checks if relative offsets match the candle pattern.
     *
     * @param dx The X offset from the center.
     * @param dz The Z offset from the center.
     * @return True if the position is a valid candle position.
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
     * Validates that every expected candle position contains a candle block.
     *
     * @param center The center location of the ritual.
     * @return True if all 16 positions contain candles.
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
     * Returns the 16 absolute candle positions surrounding the center coordinates.
     *
     * @param cx The center X coordinate.
     * @param cz The center Z coordinate.
     * @return A list of coordinate pairs representing candle positions.
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
     * Reads candle configuration using relative offsets from the center.
     *
     * @param center The center location of the ritual.
     * @return A map of relative (dx, dz) offsets to their (count, material) pairs.
     */
    private fun getCandleConfigs(center: Location): Map<Pair<Int, Int>, Pair<Int, Material>> {
        val world = center.world ?: return emptyMap()
        val cx = center.blockX
        val cy = center.blockY
        val cz = center.blockZ

        val configs = mutableMapOf<Pair<Int, Int>, Pair<Int, Material>>()

        getRitualPositions(cx, cz).forEach { (x, z) ->
            val loc = Location(world, x.toDouble(), cy.toDouble(), z.toDouble())
            val block = loc.block

            if (Tag.CANDLES.isTagged(block.type)) {
                val candleData = block.blockData as? Candle
                if (candleData != null) {
                    val dx = x - cx
                    val dz = z - cz
                    configs[dx to dz] = candleData.candles to block.type
                }
            }
        }

        return configs
    }

    /**
     * Activates a ritual once all candles are lit.
     *
     * @param circle The ritual circle to activate.
     */
    private fun activateRitual(circle: RitualCircle) {
        circle.isActive = true
        lightAllCandles(circle)
        spawnParticleTrails(circle)
    }

    /**
     * Checks if a skeleton skull is present at the center.
     *
     * @param center The center location to check.
     * @return True if a skeleton skull is present.
     */
    private fun isSkullPresent(center: Location): Boolean =
        center.block.type == Material.SKELETON_SKULL || center.block.type == Material.SKELETON_WALL_SKULL

    /**
     * Checks if all candles in the circle are lit.
     *
     * @param circle The ritual circle to check.
     * @return True if all candles are lit.
     */
    private fun areAllCandlesLit(circle: RitualCircle): Boolean =
        circle.candles.keys.all { (dx, dz) ->
            val loc = circle.center.clone().add(dx.toDouble(), 0.0, dz.toDouble())
            val lightable = loc.block.blockData as? Lightable

            lightable?.isLit == true
        }

    /**
     * Forces all candles in the circle to lit state.
     *
     * @param circle The ritual circle to light.
     */
    private fun lightAllCandles(circle: RitualCircle) {
        circle.candles.keys.forEach { (dx, dz) ->
            val block = resolveCandleLocation(circle.center, dx, dz).block

            if (!Tag.CANDLES.isTagged(block.type)) return@forEach

            val lightable = block.blockData as? Lightable ?: return@forEach

            if (!lightable.isLit) {
                lightable.isLit = true
                block.blockData = lightable
            }
        }
    }

    /**
     * Forces all candles in the circle to unlit state.
     *
     * @param circle The ritual circle to extinguish.
     */
    private fun extinguishAllCandles(circle: RitualCircle) {
        circle.candles.keys.forEach { (dx, dz) ->
            val block = resolveCandleLocation(circle.center, dx, dz).block

            if (!Tag.CANDLES.isTagged(block.type)) return@forEach

            val lightable = block.blockData as? Lightable ?: return@forEach

            if (!lightable.isLit) {
                lightable.isLit = false
                block.blockData = lightable
            }
        }
    }

    /**
     * Spawns animated particle trails from each candle toward the center.
     *
     * @param circle The ritual circle to spawn trails for.
     */
    private fun spawnParticleTrails(circle: RitualCircle) {
        val center = circle.center.block.center()
        val candles =
            circle.candles.keys.map { (dx, dz) -> resolveCandleLocation(circle.center, dx, dz) }

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
     * Finishes the activation sequence with lightning and cloud rings.
     *
     * @param circle The ritual circle to finish activating.
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
                        repeat(2) {
                            val radius = 2.0 + it
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

    /**
     * Finds the first player standing inside the circle.
     *
     * @param circle The ritual circle to check.
     * @return The first player found inside, or null if none.
     */
    private fun findPlayerInsideCircle(circle: RitualCircle): Player? =
        circle.center.world
            ?.getNearbyEntities(circle.center, 3.0, 3.0, 3.0)
            ?.filterIsInstance<Player>()
            ?.find { isPlayerInsideCircle(it, circle.center) }

    /**
     * Checks whether the player is within 3 blocks (horizontal) of the center.
     *
     * @param player The player to check.
     * @param center The center location of the ritual.
     * @return True if the player is inside the circle.
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
     * Starts a 60-tick charge-up sequence before teleporting the player.
     *
     * @param player The player to teleport.
     * @param circle The ritual circle the player is in.
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
                player.sendActionBar(MM.deserialize(Messages.TELEPORT_COUNTDOWN_3))
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1.0f, 0.8f)
            },
            0L,
        )

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                player.sendActionBar(MM.deserialize(Messages.TELEPORT_COUNTDOWN_2))
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1.0f, 1.0f)
            },
            20L,
        )

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                player.sendActionBar(MM.deserialize(Messages.TELEPORT_COUNTDOWN_1))
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
                    player.sendActionBar(MM.deserialize(Messages.PORTAL_CLOSED))
                    return@Runnable
                }
                if (!isPlayerInsideCircle(player, circle.center)) {
                    player.sendActionBar(MM.deserialize(Messages.TELEPORT_CANCELLED_LEFT_CIRCLE))
                    return@Runnable
                }

                performTeleport(player, circle)
            },
            60L,
        )
    }

    /**
     * Spawns rotating spiral particles around the player during charge-up.
     *
     * @param player The player to spawn particles around.
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

    /**
     * Draws a purple particle line between two locations.
     *
     * @param from The starting location.
     * @param to The ending location.
     */
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
     * @param circle The ritual circle to spawn the trail for.
     */
    private fun spawnPurpleCandleTrail(circle: RitualCircle) {
        val candles =
            circle.candles.keys.map { (dx, dz) -> resolveCandleLocation(circle.center, dx, dz) }

        if (candles.size < 2) return

        val center = circle.center
        val sorted =
            candles.sortedBy {
                val dx = it.x - center.x
                val dz = it.z - center.z

                atan2(dz, dx)
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
     * Spawns a tall vertical beacon beam at the location.
     *
     * @param location The location to spawn the beam at.
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
     * @param event The BlockBreakEvent to handle.
     */
    private fun blockBreak(event: BlockBreakEvent) {
        val block = event.block
        val loc = block.location

        if (Tag.CANDLES.isTagged(block.type)) {
            activeRituals.values
                .find {
                    val dx = loc.blockX - it.center.blockX
                    val dz = loc.blockZ - it.center.blockZ

                    (dx to dz) in it.candles
                }?.let {
                    deactivateRitual(it)
                    activeRituals.remove(it.center)
                }
        }
    }

    /**
     * Fully deactivates a ritual circle.
     *
     * @param circle The ritual circle to deactivate.
     */
    private fun deactivateRitual(circle: RitualCircle) {
        circle.isActive = false
        circle.activeTaskIds.forEach { instance.server.scheduler.cancelTask(it) }
        circle.activeTaskIds.clear()
        extinguishAllCandles(circle)
        circle.center.world
            ?.getNearbyEntities(circle.center, 10.0, 10.0, 10.0)
            ?.filterIsInstance<Player>()
            ?.forEach { it.sendActionBar(MM.deserialize(Messages.PORTAL_CLOSES)) }
    }

    /**
     * Teleports the player to the matching ritual if one exists.
     *
     * @param player The player to teleport.
     * @param circle The source ritual circle.
     */
    private fun performTeleport(
        player: Player,
        circle: RitualCircle,
    ) {
        val currentRitual = RitualLocation.fromLocation(circle.center, toAbsoluteMap(circle.center, circle.candles))
        val targetRitual = RitualStorageManager.findPair(currentRitual)

        if (targetRitual != null) {
            targetRitual.getCenter().let {
                spawnBeaconBeam(it.clone().add(0.5, 0.0, 0.5))
                player.teleport(it.clone().add(0.5, 0.0, 0.5))
                player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f)
                player.sendActionBar(MM.deserialize(Messages.TELEPORT_SUCCESS))
                circle.center.block.type = Material.AIR
            }
        } else {
            player.sendActionBar(MM.deserialize(Messages.NO_PAIRED_RITUAL))
        }
    }

    /**
     * Converts relative candle map to absolute locations.
     *
     * @param center The ritual center.
     * @param candles Relative candle map.
     * @return Absolute location map.
     */
    private fun toAbsoluteMap(
        center: Location,
        candles: Map<Pair<Int, Int>, Pair<Int, Material>>,
    ): Map<Location, Pair<Int, Material>> {
        val world = center.world ?: return emptyMap()
        val cx = center.blockX
        val cy = center.blockY
        val cz = center.blockZ

        return candles.mapKeys { (offset, _) ->
            val (dx, dz) = offset

            Location(world, (cx + dx).toDouble(), cy.toDouble(), (cz + dz).toDouble())
        }
    }

    /**
     * Resolves a candle location from relative offsets.
     *
     * @param center The ritual center location.
     * @param dx The relative X offset.
     * @param dz The relative Z offset.
     * @return The absolute block location.
     */
    private fun resolveCandleLocation(
        center: Location,
        dx: Int,
        dz: Int,
    ): Location =
        Location(
            center.world,
            (center.blockX + dx).toDouble(),
            center.blockY.toDouble(),
            (center.blockZ + dz).toDouble(),
        )
}
