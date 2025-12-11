package org.xodium.vanillaplus.utils

import com.sk89q.worldedit.extent.clipboard.Clipboard
import org.bukkit.Location
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.DoubleChest
import org.bukkit.block.data.type.Leaves

/** Block utilities. */
internal object BlockUtils {
    /**
     * Get the centre of a block, handling double chests properly.
     * @return The centre location of the block.
     */
    val Block.center: Location
        get() {
            val baseAddition = Location(location.world, location.x + 0.5, location.y + 0.5, location.z + 0.5)
            val chestState = state as? Chest ?: return baseAddition
            val holder = chestState.inventory.holder as? DoubleChest ?: return baseAddition
            val leftBlock = (holder.leftSide as? Chest)?.block
            val rightBlock = (holder.rightSide as? Chest)?.block

            if (leftBlock == null || rightBlock == null || leftBlock.world !== rightBlock.world) return baseAddition

            val world = leftBlock.world
            val cx = (leftBlock.x + rightBlock.x) / 2.0 + 0.5
            val cy = (leftBlock.y + rightBlock.y) / 2.0 + 0.5
            val cz = (leftBlock.z + rightBlock.z) / 2.0 + 0.5

            return Location(world, cx, cy, cz)
        }

    /**
     * Sets the persistent property of all leaves in the pasted area.
     * @param clipboard The clipboard that was pasted.
     * @param persistence Whether leaves should be persistent. Defaults to false.
     */
    fun Block.leafPersistence(
        clipboard: Clipboard,
        persistence: Boolean = false,
    ) {
        val region = clipboard.region.clone()
        val origin = clipboard.origin

        region.forEach { vector ->
            val offset = vector.subtract(origin)
            val targetBlock = world.getBlockAt(x + offset.x(), y + offset.y(), z + offset.z())

            if (Tag.LEAVES.isTagged(targetBlock.type)) {
                val blockData = targetBlock.blockData

                if (blockData is Leaves) {
                    blockData.isPersistent = persistence
                    targetBlock.setBlockData(blockData, false)
                }
            }
        }
    }
}
