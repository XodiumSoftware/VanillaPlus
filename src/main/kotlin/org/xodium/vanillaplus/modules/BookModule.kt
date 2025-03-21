/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.interfaces.ModuleInterface

/**
 * TODO: Add description
 */
class BookModule : ModuleInterface {
    /**
     * Returns true if the module is enabled in the plugin's configuration.
     */
    override fun enabled(): Boolean = Config.BookModule.ENABLED
}