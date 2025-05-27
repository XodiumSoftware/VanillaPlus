/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.registries

import org.bukkit.event.inventory.InventoryType

/** Registry for inventory types. */
object InventoryTypeRegistry {
    val CONTAINERS: Set<InventoryType> = setOf(
        InventoryType.CHEST,
        InventoryType.BARREL,
        InventoryType.ENDER_CHEST,
        InventoryType.SHULKER_BOX
    )
}