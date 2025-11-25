package org.xodium.vanillaplus.interfaces

import org.bukkit.event.Listener
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import kotlin.time.measureTime

/** Represents a contract for a feature within the system. */
internal interface FeatureInterface : Listener {
    /** Registers this feature as an event listener with the server. */
    fun register() {
        instance.logger.info(
            "Registering: ${this::class.simpleName} | Took ${
                measureTime {
                    instance.server.pluginManager.registerEvents(this, instance)
                }.inWholeMilliseconds
            }ms",
        )
    }
}
