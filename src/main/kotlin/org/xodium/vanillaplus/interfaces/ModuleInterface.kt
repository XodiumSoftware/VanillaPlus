package org.xodium.vanillaplus.interfaces

import org.bukkit.event.Listener

/**
 * This interface defines a module system that integrates with the Bukkit event handling mechanism.
 * Developers implementing this interface are required to provide a name for the module (`cn`)
 * and define whether it is enabled through the `enabled()` function.
 *
 * The implementation classes of this interface are expected to also function as Bukkit event listeners.
 */
interface ModuleInterface : Listener {

    /**
     * The unique name or identifier for the module.
     * This can be used to distinguish this module from others.
     */
    val cn: String

    /**
     * Determines if this module is currently enabled or not.
     *
     * @return `true` if the module is enabled, `false` otherwise.
     */
    fun enabled(): Boolean
}