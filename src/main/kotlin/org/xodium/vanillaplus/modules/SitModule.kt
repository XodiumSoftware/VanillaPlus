/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.interfaces.ModuleInterface

class SitModule : ModuleInterface<SitModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}