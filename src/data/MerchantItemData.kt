package org.xodium.illyriaplus.data

import org.bukkit.inventory.ItemStack

/**
 * Represents a single tradeable entry in the travelling merchant's shop GUI.
 *
 * @property result The [ItemStack] the player receives on purchase.
 * @property price The [ItemStack] (type and amount) deducted from the player's inventory on purchase.
 */
internal data class MerchantItemData(
    val result: ItemStack,
    val price: ItemStack,
)
