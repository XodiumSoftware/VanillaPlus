package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling map mechanics within the system. */
internal object MapModule : ModuleInterface {
    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
