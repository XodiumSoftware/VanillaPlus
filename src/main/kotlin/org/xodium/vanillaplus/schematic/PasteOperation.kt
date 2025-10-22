package org.xodium.vanillaplus.schematic

import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.util.Vector

/**
 * Handles the operation of pasting blocks from a clipboard to a target location.
 * @property blockStateMap A map of relative vectors to block states that will be pasted.
 */
internal class PasteOperation(
    private val blockStateMap: Map<Vector, BlockState>,
) {
    /**
     * Transforms a target block by applying the corresponding block state from the clipboard.
     * @param block The target block to transform, or null if no block exists at the location.
     */
    fun transformBlock(block: Block?) {
        block?.let {
            val vector =
                it.location.toVector().apply {
                    setX(x.toInt().toDouble())
                    setY(y.toInt().toDouble())
                    setZ(z.toInt().toDouble())
                }
            blockStateMap[vector]?.copy(block.location)?.update(true, false)
        }
    }
}
