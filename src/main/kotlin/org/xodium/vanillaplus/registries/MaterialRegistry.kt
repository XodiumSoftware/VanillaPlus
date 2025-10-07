@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.registries

import org.bukkit.Material
import java.util.*

/** Registry for materials. */
internal object MaterialRegistry {
    val CONTAINER_TYPES: EnumSet<Material> =
        EnumSet.copyOf(
            Material.entries.filter {
                it.name.endsWith("BARREL") ||
                    it.name.endsWith("CHEST") ||
                    it.name == "SHULKER_BOX" ||
                    it.name.endsWith("_SHULKER_BOX")
            },
        )

    val SAPLING_LINKS: Map<Material, List<String>> =
        mapOf(
            Material.ACACIA_SAPLING to listOf("trees/acacia"),
            Material.BIRCH_SAPLING to listOf("trees/birch"),
            Material.CHERRY_SAPLING to listOf("trees/cherry"),
            Material.CRIMSON_FUNGUS to listOf("trees/crimson"),
            Material.DARK_OAK_SAPLING to listOf("trees/dark_oak"),
            Material.JUNGLE_SAPLING to listOf("trees/jungle"),
            Material.MANGROVE_PROPAGULE to listOf("trees/mangrove"),
            Material.OAK_SAPLING to listOf("trees/oak"),
            Material.PALE_OAK_SAPLING to listOf("trees/pale_oak"),
            Material.SPRUCE_SAPLING to listOf("trees/spruce"),
            Material.WARPED_FUNGUS to listOf("trees/warped"),
        )
}
