/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.interfaces.ModuleInterface

class InvUnloadModule : ModuleInterface {
    override fun enabled(): Boolean = Config.InvUnloadModule.ENABLED
}