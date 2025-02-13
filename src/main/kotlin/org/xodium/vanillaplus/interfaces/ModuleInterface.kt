/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.interfaces

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import org.bukkit.event.Listener
import org.xodium.vanillaplus.Utils


/**
 * Represents a contract for a module within the system.
 * Every implementing module must define initialization logic and state management.
 * This interface extends the Listener interface, allowing modules to handle events.
 */
interface ModuleInterface : Listener {
    /**
     * Determines if this module is currently enabled.
     *
     * @return `true` if the module is enabled, `false` otherwise.
     */
    fun enabled(): Boolean

    /**
     * Builds the GUI for the module.
     * Default implementation returns an empty GUI.
     *
     * @return The GUI for the module.
     */
    fun gui(): Gui = buildGui { spamPreventionDuration = Utils.antiSpamDuration }
}
