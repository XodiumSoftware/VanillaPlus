/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold.utils

import org.bukkit.inventory.ItemStack

object ShulkerUtils {
    @JvmStatic
    fun isShulkerBox(itemStack: ItemStack?): Boolean {
        if (itemStack == null) return false
        return itemStack.type.name.contains("SHULKER_BOX") //TODO: use Registry/Tag
    }
}
