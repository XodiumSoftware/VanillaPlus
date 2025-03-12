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

    val SWORDS = setOf(
        Material.WOODEN_SWORD,
        Material.STONE_SWORD,
        Material.IRON_SWORD,
        Material.GOLDEN_SWORD,
        Material.DIAMOND_SWORD,
        Material.NETHERITE_SWORD,
    )
}