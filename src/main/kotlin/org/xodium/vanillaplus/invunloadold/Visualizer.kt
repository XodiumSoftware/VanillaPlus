/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.invunloadold.utils.BlockUtils
import org.xodium.vanillaplus.utils.TimeUtils
import java.util.*

object Visualizer {
    private val lastUnloads = mutableMapOf<UUID, List<Block>>()
    private val lastUnloadPositions = mutableMapOf<UUID, Location>()
    private val activeVisualizations = mutableMapOf<UUID, Int>()
    private val unloadSummaries = mutableMapOf<UUID, UnloadSummary>()

    fun save(
        player: Player,
        affectedChests: List<Block>,
        summary: UnloadSummary?
    ) {
        lastUnloads[player.uniqueId] = affectedChests
        lastUnloadPositions[player.uniqueId] = player.location.clone().add(0.0, 0.75, 0.0)
        summary?.let { unloadSummaries[player.uniqueId] = it }
    }

    fun play(p: Player): Unit? = lastUnloads[p.uniqueId]?.let { play(it as ArrayList<Block>, p) }

    private fun laserEffect(
        destinations: List<Block>,
        player: Player,
        interval: Double,
        count: Int,
        particle: Particle,
        speed: Double,
        maxDistance: Int
    ) {
        destinations.forEach { destination ->
            val start = player.location.clone()
            val end = BlockUtils.getCenterOfBlock(destination).add(0.0, -0.5, 0.0)
            val vec = getDirectionBetweenLocations(start, end)
            val distance = start.distance(destination.location)
            if (distance < maxDistance) {
                var i = 1.0
                while (i <= distance) {
                    val step = vec.clone().normalize().multiply(i)
                    start.add(step)
                    player.spawnParticle(particle, start, count, 0.0, 0.0, 0.0, speed)
                    start.subtract(step)
                    i += interval
                }
            }
        }
    }

    fun play(affectedChests: List<Block>, player: Player) {
        activeVisualizations[player.uniqueId] = instance.server.scheduler.scheduleSyncRepeatingTask(
            instance,
            { laserEffect(affectedChests, player, 0.3, 2, Particle.CRIT, 0.001, 128) },
            0L,
            2L
        )

        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                activeVisualizations[player.uniqueId]?.let {
                    instance.server.scheduler.cancelTask(it)
                    activeVisualizations.remove(player.uniqueId)
                }
            },
            TimeUtils.seconds(5)
        )
    }

    fun chestEffect(block: Block, player: Player) {
        player.spawnParticle(Particle.CRIT, BlockUtils.getCenterOfBlock(block), 10, 0.0, 0.0, 0.0)
    }

    private fun getDirectionBetweenLocations(start: Location, end: Location): Vector {
        return end.toVector().subtract(start.toVector())
    }
}
