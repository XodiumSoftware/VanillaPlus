package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling horde mechanics within the system. */
internal object HordeModule : ModuleInterface {
    @Serializable
    data class Config(
        val enabled: Boolean = true,
    )
}
