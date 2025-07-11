package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.hooks.ProtocolLibHook
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling NameTag mechanics within the system. */
class NameTagModule : ModuleInterface<NameTagModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean {
        if (!config.enabled) return false

        return ProtocolLibHook.getPlugin("ProtocolLib not found, disabling NameTagModule")
    }

    init {
        if (enabled()) ProtocolLibHook.nametag()
    }

    data class Config(
        override var enabled: Boolean = true
    ) : ModuleInterface.Config
}