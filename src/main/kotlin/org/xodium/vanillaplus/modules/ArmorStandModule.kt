package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling armor stand mechanics within the system. */
internal object ArmorStandModule : ModuleInterface {
    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
