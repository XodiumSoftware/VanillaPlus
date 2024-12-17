package org.xodium.vanillaplus.interfaces

import org.bukkit.event.Listener

interface ModuleInterface : Listener {
    fun enabled(): Boolean
}