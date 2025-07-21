package org.xodium.vanillaplus.registries

import org.bukkit.Material
import java.util.*

/** Registry for materials. */
internal object MaterialRegistry {
    val CONTAINER_TYPES: EnumSet<Material> = EnumSet.copyOf(
        Material.entries.filter {
            it.name.endsWith("BARREL") ||
                    it.name.endsWith("CHEST") ||
                    it.name == "SHULKER_BOX" ||
                    it.name.endsWith("_SHULKER_BOX")
        }
    )

    val TREE_MASK: Set<Material> = setOf(
        // General
        Material.AIR,
        // Greenery
        Material.SHORT_GRASS,
        Material.TALL_GRASS,
        Material.FERN,
        Material.LARGE_FERN,
        Material.DEAD_BUSH,
        Material.VINE,
        Material.SEAGRASS,
        Material.TALL_SEAGRASS,
        Material.SUGAR_CANE,
        Material.KELP,
        Material.KELP_PLANT,
        Material.CAVE_VINES,
        Material.CAVE_VINES_PLANT,
        Material.WEEPING_VINES,
        Material.WEEPING_VINES_PLANT,
        Material.TWISTING_VINES,
        Material.TWISTING_VINES_PLANT,
        Material.AZALEA_LEAVES,
        Material.AZALEA,
        Material.SNOW,
        Material.MOSS_CARPET,
        Material.MOSS_BLOCK,
        // Small Flowers
        Material.ALLIUM,
        Material.AZURE_BLUET,
        Material.BLUE_ORCHID,
        Material.CORNFLOWER,
        Material.DANDELION,
        Material.CLOSED_EYEBLOSSOM,
        Material.OPEN_EYEBLOSSOM,
        Material.LILY_OF_THE_VALLEY,
        Material.OXEYE_DAISY,
        Material.POPPY,
        Material.TORCHFLOWER,
        Material.ORANGE_TULIP,
        Material.PINK_TULIP,
        Material.RED_TULIP,
        Material.WHITE_TULIP,
        Material.WITHER_ROSE,
        // Tall Flowers
        Material.LILAC,
        Material.PEONY,
        Material.PITCHER_PLANT,
        Material.ROSE_BUSH,
        Material.SUNFLOWER,
        // Other Flowers
        Material.CHERRY_LEAVES,
        Material.CHORUS_FLOWER,
        Material.FLOWERING_AZALEA,
        Material.FLOWERING_AZALEA_LEAVES,
        Material.MANGROVE_PROPAGULE,
        Material.PINK_PETALS,
        Material.SPORE_BLOSSOM,
    )
}