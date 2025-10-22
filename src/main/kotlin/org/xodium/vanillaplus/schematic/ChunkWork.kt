package org.xodium.vanillaplus.schematic

import org.bukkit.Chunk
import org.bukkit.World
import java.util.concurrent.CompletableFuture

/**
 * Represents work to be done on a specific chunk for asynchronous chunk loading operations.
 * @property chunkX The X coordinate of the chunk.
 * @property chunkZ The Z coordinate of the chunk.
 */
internal data class ChunkWork(
    val chunkX: Int,
    val chunkZ: Int,
) {
    val blockX: Int get() = chunkX shl 4
    val blockZ: Int get() = chunkZ shl 4

    /**
     * Asynchronously loads the chunk from the specified world.
     * @param world The world from which to load the chunk.
     * @return A CompletableFuture that will contain the loaded chunk, or null if the chunk couldn't be loaded.
     */
    fun getChunkAsync(world: World): CompletableFuture<Chunk?> = world.getChunkAtAsync(chunkX, chunkZ, true)
}
