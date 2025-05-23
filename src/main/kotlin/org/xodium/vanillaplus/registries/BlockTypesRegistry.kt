/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.registries

import com.sk89q.worldedit.world.block.BlockType
import com.sk89q.worldedit.world.block.BlockTypes

/** Registry for blocktypes. */
object BlockTypesRegistry {
    val TREE_MASK: Set<BlockType?> = setOf(
        // General
        BlockTypes.AIR,
        // Greenery
        BlockTypes.SHORT_GRASS,
        BlockTypes.TALL_GRASS,
        BlockTypes.FERN,
        BlockTypes.LARGE_FERN,
        BlockTypes.DEAD_BUSH,
        BlockTypes.VINE,
        BlockTypes.SEAGRASS,
        BlockTypes.TALL_SEAGRASS,
        BlockTypes.SUGAR_CANE,
        BlockTypes.KELP,
        BlockTypes.KELP_PLANT,
        BlockTypes.CAVE_VINES,
        BlockTypes.CAVE_VINES_PLANT,
        BlockTypes.WEEPING_VINES,
        BlockTypes.WEEPING_VINES_PLANT,
        BlockTypes.TWISTING_VINES,
        BlockTypes.TWISTING_VINES_PLANT,
        BlockTypes.AZALEA_LEAVES,
        BlockTypes.AZALEA,
        BlockTypes.SNOW,
        BlockTypes.MOSS_CARPET,
        BlockTypes.MOSS_BLOCK,
        // Small Flowers
        BlockTypes.ALLIUM,
        BlockTypes.AZURE_BLUET,
        BlockTypes.BLUE_ORCHID,
        BlockTypes.CORNFLOWER,
        BlockTypes.DANDELION,
        BlockTypes.CLOSED_EYEBLOSSOM,
        BlockTypes.OPEN_EYEBLOSSOM,
        BlockTypes.LILY_OF_THE_VALLEY,
        BlockTypes.OXEYE_DAISY,
        BlockTypes.POPPY,
        BlockTypes.TORCHFLOWER,
        BlockTypes.ORANGE_TULIP,
        BlockTypes.PINK_TULIP,
        BlockTypes.RED_TULIP,
        BlockTypes.WHITE_TULIP,
        BlockTypes.WITHER_ROSE,
        // Tall Flowers
        BlockTypes.LILAC,
        BlockTypes.PEONY,
        BlockTypes.PITCHER_PLANT,
        BlockTypes.ROSE_BUSH,
        BlockTypes.SUNFLOWER,
        // Other Flowers
        BlockTypes.CHERRY_LEAVES,
        BlockTypes.CHORUS_FLOWER,
        BlockTypes.FLOWERING_AZALEA,
        BlockTypes.FLOWERING_AZALEA_LEAVES,
        BlockTypes.MANGROVE_PROPAGULE,
        BlockTypes.PINK_PETALS,
        BlockTypes.SPORE_BLOSSOM,
    )
}