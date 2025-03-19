/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface

class InvUnloadModule : ModuleInterface {
    /**
     * @return true if the module is enabled
     */
    override fun enabled(): Boolean = ConfigData.InvUnloadModule().enabled
}