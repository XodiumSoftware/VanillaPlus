package org.xodium.illyriaplus.interfaces

import org.bukkit.inventory.ItemStack

/** Represents a contract for custom items within the system. */
internal interface ItemInterface {
    /** The [ItemStack] representing this custom item. */
    val item: ItemStack
}
