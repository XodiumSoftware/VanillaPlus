package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling cauldron mechanics within the system. */
internal class CauldronModule : ModuleInterface<CauldronModule.Config> {
    override val config: Config = Config()

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
