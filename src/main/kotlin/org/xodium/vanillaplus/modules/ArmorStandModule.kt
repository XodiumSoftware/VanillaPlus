package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling armor stand mechanics within the system. */
internal class ArmorStandModule : ModuleInterface<ArmorStandModule.Config> {
    override val config: Config = Config()

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
