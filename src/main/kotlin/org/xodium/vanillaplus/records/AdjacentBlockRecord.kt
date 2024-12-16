package org.xodium.vanillaplus.records

import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Door.Hinge
import java.util.*

@JvmRecord
data class AdjacentBlockRecord(
    @JvmField val offsetX: Int,
    @JvmField val offsetZ: Int,
    @JvmField val hinge: Hinge?,
    @JvmField val facing: BlockFace?
) {
    init {
        Objects.requireNonNull<Hinge?>(hinge)
        Objects.requireNonNull<BlockFace?>(facing)
    }
}