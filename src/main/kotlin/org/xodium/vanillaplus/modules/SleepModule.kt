package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling sleeping mechanics within the system. */
class SleepModule : ModuleInterface<SleepModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}