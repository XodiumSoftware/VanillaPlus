package org.xodium.vanillaplus.utils

import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.Container
import org.bukkit.block.DoubleChest
import org.bukkit.inventory.Inventory

/** Block utilities. */
internal object BlockUtils {
    /**
     * Get the centre of a block, handling double chests properly.
     * @return The centre location of the block.
     */
    fun Block.center(): Location {
        val loc = location.clone()
        val stateChest = state as? Chest ?: return loc.add(0.5, 0.5, 0.5)
        val holder = stateChest.inventory.holder as? DoubleChest
        if (holder != null) {
            val leftLoc = (holder.leftSide as? Chest)?.block?.location
            val rightLoc = (holder.rightSide as? Chest)?.block?.location
            if (leftLoc != null && rightLoc != null) {
                loc.x = (leftLoc.x + rightLoc.x) / 2.0 + 0.5
                loc.y = (leftLoc.y + rightLoc.y) / 2.0 + 0.5
                loc.z = (leftLoc.z + rightLoc.z) / 2.0 + 0.5
                return loc.add(0.5, 0.5, 0.5)
            }
        }
        return loc.add(0.5, 0.5, 0.5)
    }

    /**
     * Check if a block is a container.
     * @return True if the block is a container.
     */
    fun Block.isContainer(): Boolean = state is Container

    /**
     * Get the container inventory if the block is a container.
     * @return The container inventory or null.
     */
    fun Block.getContainerInventory(): Inventory? = (state as? Container)?.inventory
}
