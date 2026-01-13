package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling quest mechanics within the system. */
internal object QuestModule : ModuleInterface {
    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
