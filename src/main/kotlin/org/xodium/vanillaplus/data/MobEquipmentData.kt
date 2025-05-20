/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * Data class to hold information about mob equipment.
 * @property slot The equipment slot of the item.
 * @property item The item stack representing the equipment.
 * @property dropChance The chance of the item dropping.
 */
data class MobEquipmentData(
    val slot: EquipmentSlot,
    val item: ItemStack,
    val dropChance: Float
)
