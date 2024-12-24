package org.xodium.vanillaplus.interfaces

import org.bukkit.event.Listener


/**
 * Represents a contract for a module within the system.
 * Every implementing module must define initialization logic and state management.
 * This interface extends the Listener interface, allowing modules to handle events.
 */
interface ModuleInterface : Listener {

    /**
     * The unique name or identifier for the module.
     */
    val cn: String

    /**
     * Initializes the functionality of the implementing module.
     */
    fun init()

    /**
     * Determines if this module is currently enabled.
     *
     * @return `true` if the module is enabled, `false` otherwise.
     */
    fun enabled(): Boolean

    /**
     * Determines if this module has dependencies.
     *
     * @return `true` if it has dependencies, `false` otherwise.
     */
    fun hasDependencies(): Boolean
}