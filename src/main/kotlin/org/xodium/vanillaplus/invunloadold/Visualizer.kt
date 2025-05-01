/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.invunloadold.utils.BlockUtils
import java.util.*

object Visualizer {
    private val lastUnloads: HashMap<UUID?, ArrayList<Block?>?> =
        HashMap<UUID?, ArrayList<Block?>?>()
    private val lastUnloadPositions: HashMap<UUID?, Location?> =
        HashMap<UUID?, Location?>()
    val activeVisualizations: HashMap<UUID?, Int?> = HashMap<UUID?, Int?>()
    private val unloadSummaries: HashMap<UUID?, UnloadSummary?> = HashMap<UUID?, UnloadSummary?>()

    fun save(
        p: Player,
        affectedChests: ArrayList<Block?>?,
        summary: UnloadSummary?
    ) {
        lastUnloads.put(p.uniqueId, affectedChests)
        lastUnloadPositions.put(p.uniqueId, p.location.add(0.0, 0.75, 0.0))
        unloadSummaries.put(p.uniqueId, summary)
    }

    fun play(p: Player) {
        if (lastUnloads.containsKey(p.uniqueId)) {
            play(lastUnloads.get(p.uniqueId) as ArrayList<Block>, p)
        }
    }

    private fun play(
        destinations: ArrayList<Block>,
        p: Player,
        interval: Double,
        count: Int,
        particle: Particle,
        speed: Double,
        maxDistance: Int
    ) {
        for (destination in destinations) {
            val start = p.location
            val vec: org.bukkit.util.Vector = getDirectionBetweenLocations(
                start,
                BlockUtils.getCenterOfBlock(destination).add(0.0, -0.5, 0.0)
            )
            if (start.distance(destination.location) < maxDistance) {
                var i = 1.0
                while (i <= start.distance(destination.location)) {
                    vec.multiply(i)
                    start.add(vec)
                    p.spawnParticle(particle, start, count, 0.0, 0.0, 0.0, speed)
                    start.subtract(vec)
                    vec.normalize()
                    i += interval
                }
            }
        }
    }

    fun play(affectedChests: ArrayList<Block>, p: Player) {
        val particle: Particle =
            Particle.valueOf(instance.config.getString("laser-particle", "CRIT")!!.uppercase(Locale.getDefault()))
        val count = instance.config.getInt("laser-count", 1)
        val maxDistance = instance.config.getInt("laser-max-distance", 128)
        val interval = instance.config.getDouble("laser-interval", 0.3)
        val speed = instance.config.getDouble("laser-speed", 0.001)

        val task: Int = instance.server.scheduler.scheduleSyncRepeatingTask(
            instance,
            { play(affectedChests, p, interval, count, particle, speed, maxDistance) },
            0,
            2
        )

        activeVisualizations.put(p.uniqueId, task)

        object : BukkitRunnable() {
            override fun run() {
                instance.server.scheduler.cancelTask(task)
                activeVisualizations.remove(p.uniqueId)
            }
        }.runTaskLater(instance, 100)
    }

    fun chestAnimation(block: Block?, player: Player) {
        val loc = BlockUtils.getCenterOfBlock(block!!)

        if (instance.config.getBoolean("spawn-particles")) {
            if (instance.config.getBoolean("error-particles")) {
                instance.logger.warning(
                    "Cannot spawn particles, because particle type \"" + instance.config
                        .getString("particle-type") + "\" does not exist! Please check your config.yml"
                )
            } else {
                val particleCount = instance.config.getInt("particle-count")
                val particle: Particle =
                    Particle.valueOf(instance.config.getString("particle-type")!!.uppercase(Locale.getDefault()))
                player.spawnParticle(particle, loc, particleCount, 0.0, 0.0, 0.0)
            }
        }
    }

    private fun getDirectionBetweenLocations(
        start: Location,
        end: Location
    ): Vector {
        return end.toVector().subtract(start.toVector())
    }
}
