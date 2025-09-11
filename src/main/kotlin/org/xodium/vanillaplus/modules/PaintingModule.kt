package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling painting mechanics within the system. */
internal class PaintingModule : ModuleInterface<PaintingModule.Config> {
    override val config: Config = Config()

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
