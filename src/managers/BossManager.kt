package org.xodium.illyriaplus.managers

import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.interfaces.BossInterface
import kotlin.random.Random

/**
 * Manages active boss instances, their tick updates, and natural spawning.
 */
internal object BossManager : Listener {
    private val activeBosses = mutableMapOf<LivingEntity, BossInterface>()
    private val activeBiomes = mutableSetOf<Biome>()
    private val bossByBiome = mutableMapOf<Biome, BossInterface>()

    /**
     * Registers a spawned boss for tick updates and biome tracking.
     *
     * @param entity The boss entity.
     * @param boss The boss interface instance.
     * @param biome The biome where the boss spawned.
     */
    fun registerBoss(
        entity: LivingEntity,
        boss: BossInterface,
        biome: Biome,
    ) {
        activeBosses[entity] = boss
        activeBiomes.add(biome)
    }

    /**
     * Registers a boss type for a specific biome (used for natural spawning).
     *
     * @param biome The biome where this boss spawns.
     * @param boss The boss interface instance.
     */
    fun registerBiomeBoss(
        biome: Biome,
        boss: BossInterface,
    ) {
        bossByBiome[biome] = boss
    }

    /**
     * Unregisters a boss (e.g., on death or despawn).
     *
     * @param entity The boss entity.
     */
    fun unregisterBoss(entity: LivingEntity) {
        val boss = activeBosses.remove(entity)
        boss?.let {
            activeBiomes.remove(it.biome)
        }
    }

    /**
     * Checks if a biome already has an active boss.
     *
     * @param biome The biome to check.
     * @return True if a boss is active in this biome.
     */
    fun hasActiveBossInBiome(biome: Biome): Boolean = activeBiomes.contains(biome)

    /**
     * Attempts to spawn a boss in the given biome at a valid location.
     *
     * @param biome The target biome.
     * @param world The world to spawn in.
     * @return True if a boss was spawned.
     */
    fun trySpawnBossInBiome(biome: Biome, world: World): Boolean {
        if (hasActiveBossInBiome(biome)) return false

        val boss = bossByBiome[biome] ?: return false

        // Find a suitable location in the biome
        val location = findValidSpawnLocation(world, biome) ?: return false

        boss.spawn(location)
        return true
    }

    /**
     * Finds a valid spawn location within the given biome.
     *
     * @param world The world to search in.
     * @param biome The target biome.
     * @return A valid spawn location or null if none found.
     */
    private fun findValidSpawnLocation(world: World, biome: Biome): Location? {
        // Get a random loaded chunk and check if it's the right biome
        val loadedChunks = world.loadedChunks.toList()
        if (loadedChunks.isEmpty()) return null

        // Try up to 10 random chunks
        repeat(10) {
            val chunk = loadedChunks.random()
            val location = findValidLocationInChunk(chunk, biome)
            if (location != null) return location
        }

        return null
    }

    /**
     * Finds a valid spawn location within a specific chunk that matches the biome.
     *
     * @param chunk The chunk to search.
     * @param targetBiome The target biome.
     * @return A valid spawn location or null.
     */
    private fun findValidLocationInChunk(chunk: Chunk, targetBiome: Biome): Location? {
        // Check a few random positions in the chunk
        repeat(5) {
            val x = chunk.x * 16 + Random.nextInt(16)
            val z = chunk.z * 16 + Random.nextInt(16)

            // Get the highest block at this x,z
            val y = chunk.world.getHighestBlockYAt(x, z)
            val location = Location(chunk.world, x.toDouble() + 0.5, y.toDouble() + 1.0, z.toDouble() + 0.5)

            // Check if this location is in the target biome
            if (chunk.world.getBiome(x, y, z) == targetBiome) {
                // Basic validation: ensure it's not underwater or in a wall
                val block = location.block
                val below = location.clone().subtract(0.0, 1.0, 0.0).block

                if (!block.type.isSolid && below.type.isSolid) {
                    return location
                }
            }
        }

        return null
    }

    /**
     * Starts the tick scheduler. Should be called once on plugin enable.
     */
    fun startTickScheduler() {
        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable {
                activeBosses.toMap().forEach { (entity, boss) ->
                    if (entity.isValid && !entity.isDead) {
                        boss.onTick(entity)
                    } else {
                        unregisterBoss(entity)
                    }
                }
            },
            0L,
            1L,
        )
    }

    /**
     * Handles chunk loading to potentially spawn bosses.
     * Chance to check for spawn: 5% per chunk load.
     */
    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        // Only check 5% of chunk loads to avoid performance issues
        if (Random.nextDouble() > 0.05) return

        val world = event.world
        val chunk = event.chunk

        // Check each biome type in this chunk
        val biomesInChunk = mutableSetOf<Biome>()

        for (x in 0..15 step 4) {
            for (z in 0..15 step 4) {
                val worldX = chunk.x * 16 + x
                val worldZ = chunk.z * 16 + z
                val y = world.getHighestBlockYAt(worldX, worldZ)
                val biome = world.getBiome(worldX, y, worldZ)
                biomesInChunk.add(biome)
            }
        }

        // Try to spawn bosses for any biomes in this chunk that don't have one
        for (biome in biomesInChunk) {
            if (!hasActiveBossInBiome(biome) && bossByBiome.containsKey(biome)) {
                // Additional random check - only 20% chance to actually spawn
                if (Random.nextDouble() <= 0.20) {
                    trySpawnBossInBiome(biome, world)
                }
            }
        }
    }
}
