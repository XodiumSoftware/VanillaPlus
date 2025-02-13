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