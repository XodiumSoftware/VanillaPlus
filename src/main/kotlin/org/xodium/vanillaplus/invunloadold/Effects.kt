/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.invunloadold.utils.BlockUtils
import org.xodium.vanillaplus.utils.TimeUtils
import java.util.*

object Effects {
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

    fun play(player: Player, affectedChests: List<Block>? = null) {
        val chests = affectedChests ?: lastUnloads[player.uniqueId] ?: return

        activeVisualizations[player.uniqueId] = instance.server.scheduler.scheduleSyncRepeatingTask(
            instance,
            { laserEffect(chests, player, 0.3, 2, Particle.CRIT, 0.001, 128) },
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
            val direction = end.toVector().subtract(start.toVector()).normalize()
            val distance = start.distance(destination.location)
            if (distance < maxDistance) {
                var i = 1.0
                while (i <= distance) {
                    val point = start.clone().add(direction.clone().multiply(i))
                    player.spawnParticle(particle, point, count, 0.0, 0.0, 0.0, speed)
                    i += interval
                }
            }
        }
    }

    fun chestEffect(block: Block, player: Player) {
        player.spawnParticle(Particle.CRIT, BlockUtils.getCenterOfBlock(block), 10, 0.0, 0.0, 0.0)
    }
}
