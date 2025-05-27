/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.registries

import org.bukkit.Material
import java.util.*

/** Registry for materials. */
object MaterialRegistry {
    val CONTAINER_TYPES: EnumSet<Material> = EnumSet.copyOf(
        Material.entries.filter {
            it.name.endsWith("BARREL") ||
                    it.name.endsWith("CHEST") ||
                    it.name == "SHULKER_BOX" ||
                    it.name.endsWith("_SHULKER_BOX")
        }
    )
}