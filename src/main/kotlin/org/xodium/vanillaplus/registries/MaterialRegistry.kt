/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.registries

import org.bukkit.Material

/**
 * Registry for materials.
 */
object MaterialRegistry {
    val AXES = setOf(
        Material.DIAMOND_AXE,
        Material.GOLDEN_AXE,
        Material.IRON_AXE,
        Material.NETHERITE_AXE,
        Material.STONE_AXE,
        Material.WOODEN_AXE
    )

    val BASE_DAMAGE_MAP = mapOf(
        Material.NETHERITE_AXE to 10.0,
        Material.IRON_AXE to 9.0,
        Material.STONE_AXE to 9.0,
        Material.DIAMOND_AXE to 9.0,
        Material.NETHERITE_SWORD to 8.0,
        Material.DIAMOND_SWORD to 7.0,
        Material.WOODEN_AXE to 7.0,
        Material.GOLDEN_AXE to 7.0,
        Material.IRON_SWORD to 6.0,
        Material.STONE_SWORD to 5.0,
        Material.GOLDEN_SWORD to 4.0,
        Material.WOODEN_SWORD to 4.0,
    )

    val DEFAULT_MATERIALS = setOf(
        Material.DIAMOND_PICKAXE,
        Material.DIAMOND_AXE,
        Material.DIAMOND_HOE,
        Material.DIAMOND_SHOVEL,

        Material.GOLDEN_PICKAXE,
        Material.GOLDEN_AXE,
        Material.GOLDEN_HOE,
        Material.GOLDEN_SHOVEL,

        Material.IRON_PICKAXE,
        Material.IRON_AXE,
        Material.IRON_HOE,
        Material.IRON_SHOVEL,

        Material.STONE_PICKAXE,
        Material.STONE_AXE,
        Material.STONE_HOE,
        Material.STONE_SHOVEL,

        Material.WOODEN_PICKAXE,
        Material.WOODEN_AXE,
        Material.WOODEN_HOE,
        Material.WOODEN_SHOVEL,

        Material.SHEARS
    )

    val HOES = setOf(
        Material.DIAMOND_HOE,
        Material.GOLDEN_HOE,
        Material.IRON_HOE,
        Material.NETHERITE_HOE,
        Material.STONE_HOE,
        Material.WOODEN_HOE
    )

    val INSTA_BREAKABLE_BY_HAND = setOf(
        Material.ALLIUM,
        Material.AZURE_BLUET,
        Material.BEETROOTS,
        Material.BIRCH_SAPLING,
        Material.BLUE_ORCHID,
        Material.BRAIN_CORAL,
        Material.BROWN_MUSHROOM,
        Material.BUBBLE_CORAL,
        Material.CARROTS,
        Material.COMPARATOR,
        Material.CORNFLOWER,
        Material.CRIMSON_FUNGUS,
        Material.CRIMSON_ROOTS,
        Material.DANDELION,
        Material.DARK_OAK_SAPLING,
        Material.DEAD_BRAIN_CORAL,
        Material.DEAD_BUBBLE_CORAL,
        Material.DEAD_BUSH,
        Material.DEAD_FIRE_CORAL,
        Material.DEAD_HORN_CORAL,
        Material.DEAD_TUBE_CORAL,
        Material.FIRE_CORAL,
        Material.FLOWER_POT,
        Material.SHORT_GRASS,
        Material.HONEY_BLOCK,
        Material.HORN_CORAL,
        Material.LILAC,
        Material.LILY_OF_THE_VALLEY,
        Material.LILY_PAD,
        Material.MELON_STEM,
        Material.NETHER_WART,
        Material.OAK_SAPLING,
        Material.ORANGE_TULIP,
        Material.OXEYE_DAISY,
        Material.PEONY,
        Material.PINK_TULIP,
        Material.POPPY,
        Material.POTATOES,
        Material.POTTED_ALLIUM,
        Material.POTTED_AZURE_BLUET,
        Material.POTTED_BLUE_ORCHID,
        Material.POTTED_CORNFLOWER,
        Material.POTTED_DANDELION,
        Material.POTTED_LILAC,
        Material.POTTED_LILY_OF_THE_VALLEY,
        Material.POTTED_ORANGE_TULIP,
        Material.POTTED_OXEYE_DAISY,
        Material.POTTED_PEONY,
        Material.POTTED_PINK_TULIP,
        Material.POTTED_POPPY,
        Material.POTTED_ROSE_BUSH,
        Material.POTTED_SUNFLOWER,
        Material.POTTED_RED_TULIP,
        Material.POTTED_WHITE_TULIP,
        Material.POTTED_WITHER_ROSE,
        Material.PUMPKIN_STEM,
        Material.REDSTONE_TORCH,
        Material.REDSTONE_WALL_TORCH,
        Material.REDSTONE_WIRE,
        Material.RED_MUSHROOM,
        Material.RED_TULIP,
        Material.REPEATER,
        Material.ROSE_BUSH,
        Material.SCAFFOLDING,
        Material.SLIME_BLOCK,
        Material.SOUL_TORCH,
        Material.SOUL_WALL_TORCH,
        Material.SPRUCE_SAPLING,
        Material.SUGAR_CANE,
        Material.SUNFLOWER,
        Material.TALL_GRASS,
        Material.TNT,
        Material.TORCH,
        Material.TRIPWIRE,
        Material.TRIPWIRE_HOOK,
        Material.TUBE_CORAL,
        Material.TWISTING_VINES,
        Material.WALL_TORCH,
        Material.WARPED_FUNGUS,
        Material.WARPED_ROOTS,
        Material.WEEPING_VINES,
        Material.WHEAT,
        Material.WHITE_TULIP,
        Material.WITHER_ROSE
    )

    val PICKAXES = setOf(
        Material.DIAMOND_PICKAXE,
        Material.GOLDEN_PICKAXE,
        Material.IRON_PICKAXE,
        Material.NETHERITE_PICKAXE,
        Material.STONE_PICKAXE,
        Material.WOODEN_PICKAXE
    )

    val PROFITS_FROM_FORTUNE = setOf(TODO())

    val PROFITS_FROM_SILK_TOUCH = setOf(
        Material.BLACK_STAINED_GLASS,
        Material.BLUE_STAINED_GLASS,
        Material.BROWN_STAINED_GLASS,
        Material.CYAN_STAINED_GLASS,
        Material.ENDER_CHEST,
        Material.GLASS,
        Material.GLOWSTONE,
        Material.GRAY_STAINED_GLASS,
        Material.GREEN_STAINED_GLASS,
        Material.LIGHT_BLUE_STAINED_GLASS,
        Material.LIGHT_GRAY_STAINED_GLASS,
        Material.LIME_STAINED_GLASS,
        Material.MAGENTA_STAINED_GLASS,
        Material.NETHER_GOLD_ORE,
        Material.ORANGE_STAINED_GLASS,
        Material.PINK_STAINED_GLASS,
        Material.PURPLE_STAINED_GLASS,
        Material.QUARTZ,
        Material.RED_STAINED_GLASS,
        Material.SEA_LANTERN,
        Material.SPAWNER,
        Material.WHITE_STAINED_GLASS,
        Material.YELLOW_STAINED_GLASS
    )

    val NETHERITE_TOOLS = setOf(
        Material.NETHERITE_AXE,
        Material.NETHERITE_HOE,
        Material.NETHERITE_PICKAXE,
        Material.NETHERITE_SHOVEL
    )

    val SAPLINGS = setOf(
        Material.ACACIA_SAPLING,
        Material.BIRCH_SAPLING,
        Material.CHERRY_SAPLING,
        Material.DARK_OAK_SAPLING,
        Material.JUNGLE_SAPLING,
        Material.MANGROVE_PROPAGULE,
        Material.OAK_SAPLING,
        Material.PALE_OAK_SAPLING,
        Material.SPRUCE_SAPLING,
    )

    val SHOVELS = setOf(
        Material.DIAMOND_SHOVEL,
        Material.GOLDEN_SHOVEL,
        Material.IRON_SHOVEL,
        Material.NETHERITE_SHOVEL,
        Material.STONE_SHOVEL,
        Material.WOODEN_SHOVEL
    )

    val SWORDS = setOf(
        Material.WOODEN_SWORD,
        Material.STONE_SWORD,
        Material.IRON_SWORD,
        Material.GOLDEN_SWORD,
        Material.DIAMOND_SWORD,
        Material.NETHERITE_SWORD,
    )

    val WEAPONS = setOf(
        Material.BOW,
        Material.CROSSBOW,
        Material.DIAMOND_SWORD,
        Material.GOLDEN_SWORD,
        Material.IRON_SWORD,
        Material.MACE,
        Material.NETHERITE_SWORD,
        Material.STONE_SWORD,
        Material.TRIDENT,
        Material.WOODEN_SWORD
    )
}