@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.registries

import org.bukkit.Material

/** Registry for materials. */
internal object MaterialRegistry {
    val SAPLING_LINKS: Map<Material, String> =
        mapOf(
            Material.ACACIA_SAPLING to "trees/acacia",
            Material.BIRCH_SAPLING to "trees/birch",
            Material.CHERRY_SAPLING to "trees/cherry",
            Material.CRIMSON_FUNGUS to "trees/crimson",
            Material.DARK_OAK_SAPLING to "trees/dark_oak",
            Material.JUNGLE_SAPLING to "trees/jungle",
            Material.MANGROVE_PROPAGULE to "trees/mangrove",
            Material.OAK_SAPLING to "trees/oak",
            Material.PALE_OAK_SAPLING to "trees/pale_oak",
            Material.SPRUCE_SAPLING to "trees/spruce",
            Material.WARPED_FUNGUS to "trees/warped",
        )
}
