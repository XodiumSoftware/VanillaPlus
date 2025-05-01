/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold

import org.bukkit.Bukkit
import org.bukkit.event.Listener

class PlayerListener internal constructor(val main: Main?) : Listener {
    fun test() {
        val p = Bukkit.getPlayer("asd")
        p!!.openInventory(p.inventory)
    }
}
