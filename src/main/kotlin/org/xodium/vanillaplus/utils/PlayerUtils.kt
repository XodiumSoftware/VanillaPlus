@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.utils

import org.bukkit.Chunk
import org.bukkit.block.Container
import org.bukkit.entity.Player

/** Player utilities. */
internal object PlayerUtils {
    /**
     * Get containers around a player (3x3 area).
     * @param player The player.
     * @return Collection of containers around the player.
     */
    fun getContainersAroundPlayer(player: Player): Set<Container> =
        buildSet {
            for (chunk in getChunksAroundPlayer(player)) {
                for (state in chunk.tileEntities) {
                    if (state is Container) add(state)
                }
            }
        }

    /**
     * Get chunks around a player (3x3 area).
     * @param player The player.
     * @return Collection of chunks around the player.
     */
    fun getChunksAroundPlayer(player: Player): Set<Chunk> {
        val (baseX, baseZ) = player.location.chunk.run { x to z }

        return buildSet {
            for (x in -1..1) {
                for (z in -1..1) {
                    add(player.world.getChunkAt(baseX + x, baseZ + z))
                }
            }
        }
    }
}
