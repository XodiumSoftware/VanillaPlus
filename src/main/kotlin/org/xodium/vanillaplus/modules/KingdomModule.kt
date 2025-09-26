@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import org.xodium.vanillaplus.data.KingdomData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.util.*

/** Represents a module handling kingdom mechanics within the system. */
internal class KingdomModule : ModuleInterface<KingdomModule.Config> {
    override val config: Config = Config()

    private val kingdoms: MutableMap<UUID, KingdomData> = mutableMapOf()
    private val playerKingdoms: MutableMap<UUID, UUID> = mutableMapOf()

    data class Config(
        override var enabled: Boolean = true,
    ) : ModuleInterface.Config
}
