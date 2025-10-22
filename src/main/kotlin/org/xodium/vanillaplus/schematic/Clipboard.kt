package org.xodium.vanillaplus.schematic

import org.bukkit.Location
import org.bukkit.block.BlockState
import org.bukkit.util.Vector

/**
 * Represents a clipboard for storing and managing copied block data.
 * @property location The reference location from which the blocks were copied, or null if the clipboard is empty.
 * @property blocks A map of relative vectors to block states representing the copied blocks.
 */
internal class Clipboard(
    val location: Location? = null,
    private val blocks: Map<Vector, BlockState> = emptyMap(),
) {
    val isEmpty: Boolean get() = blocks.isEmpty()
    val size: Int get() = blocks.size

    /**
     * Retrieves an immutable copy of the blocks stored in the clipboard.
     * @return A map containing the relative vectors and their corresponding block states.
     */
    fun getBlocks(): Map<Vector, BlockState> = blocks

    companion object {
        /**
         * Creates a new Clipboard instance from the specified blocks and reference location.
         * @param location The reference location from which the blocks were copied.
         * @param blocks A map of relative vectors to block states representing the copied blocks.
         * @return A new Clipboard instance containing the provided blocks and location.
         */
        fun fromBlocks(
            location: Location,
            blocks: Map<Vector, BlockState>,
        ) = Clipboard(location, blocks)
    }
}
