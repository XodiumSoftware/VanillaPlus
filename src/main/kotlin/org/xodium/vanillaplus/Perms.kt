/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

/**
 * Permissions for the VanillaPlus plugin.
 */
object Perms {
    /**
     * Permissions for General commands.
     */
    object VanillaPlus {
        const val USE = "vanillaplus.use"
        const val RELOAD = "vanillaplus.reload"
    }

    /**
     * Permissions for Gui commands.
     */
    object GuiModule {
        const val FAQ = "vanillaplus.gui.faq"
        const val DIMS = "vanillaplus.gui.dims"
        const val SETTINGS = "vanillaplus.gui.settings"
    }
}