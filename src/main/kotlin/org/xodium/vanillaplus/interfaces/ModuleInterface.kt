/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.interfaces

import org.bukkit.event.Listener


/**
 * Represents a contract for a module within the system.
 * Every implementing module must define initialization logic and state management.
 * This interface extends the Listener interface, allowing modules to handle events.
 */
interface ModuleInterface : Listener {
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
}
