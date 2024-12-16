package org.xodium.vanillaplus.interfaces

import org.bukkit.event.Listener

interface ModuleInterface : Listener {
    interface CONFIG {
        companion object {
            const val ENABLE: String = ".enable"
        }
    }

    fun enabled(): Boolean

    fun config()
}