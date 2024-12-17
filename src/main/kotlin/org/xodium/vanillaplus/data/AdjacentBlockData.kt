package org.xodium.vanillaplus.data

import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Door.Hinge

data class AdjacentBlockData(
    val offsetX: Int,
    val offsetZ: Int,
    val hinge: Hinge,
    val facing: BlockFace
)