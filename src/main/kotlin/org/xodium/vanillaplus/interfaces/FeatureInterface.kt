package org.xodium.vanillaplus.interfaces

import org.bukkit.event.Listener
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Represents a contract for a feature within the system. */
internal interface FeatureInterface : Listener {
    /** Registers this feature as an event listener with the server. */
    fun register() = instance.server.pluginManager.registerEvents(this, instance)
}
