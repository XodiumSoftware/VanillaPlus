/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.interfaces

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener


/**
 * Represents a contract for a module within the system.
 * Every implementing module must define initialization logic and state management.
 * This interface extends the Listener interface, allowing modules to handle events.
 */
interface ModuleInterface<T : Event> : Listener {
    /**
     * Determines if this module is currently enabled.
     *
     * @return `true` if the module is enabled, `false` otherwise.
     */
    fun enabled(): Boolean

    /**
     * Initializes the functionality of the implementing module.
     * Default implementation does nothing.
     */
    fun init() = Unit

    /**
     * Handles a given Bukkit event.
     * This method is annotated with @EventHandler to ensure it is properly registered with the Bukkit event system.
     *
     * @param event The Bukkit event to be handled. This can be any event that extends org.bukkit.event.Event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: T) = Unit

    /**
     * Builds the GUI for the module.
     * Default implementation returns an empty GUI.
     *
     * @return The GUI for the module.
     */
    fun gui(): Gui = buildGui { }
}
