/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager

class RtpModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.rtpModule.enabled
}