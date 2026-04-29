package org.xodium.illyriaplus.interfaces

import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.IllyriaKingdoms.Companion.instance
import kotlin.time.measureTime

/** Represents a contract for items within the IllyriaKingdoms system. */
internal interface ItemInterface : Listener {
    /** The configured ItemStack representing this item. */
    val item: ItemStack

    /**
     * Registers this feature with the server.
     * @return The time taken to register the feature in milliseconds.
     */
    fun register(): Long =
        measureTime {
            instance.server.pluginManager.registerEvents(this, instance)
        }.inWholeMilliseconds
}
