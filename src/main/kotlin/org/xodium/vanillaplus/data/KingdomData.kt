@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.data

import org.xodium.vanillaplus.enums.KingdomTypeEnum
import org.xodium.vanillaplus.interfaces.DataInterface
import java.util.*

/**
 * Represents a kingdom in the VanillaPlus plugin.
 * @property name The display name of the kingdom.
 * @property ruler The UUID of the ruler of this kingdom.
 * @property type The type of government system for this kingdom.
 * @property members Mutable set of UUIDs representing kingdom members (excluding the ruler).
 */
internal data class KingdomData(
    val name: String,
    val ruler: UUID,
    val type: KingdomTypeEnum = KingdomTypeEnum.FEUDALISM,
    val members: MutableSet<UUID> = mutableSetOf(),
) {
    companion object : DataInterface<KingdomData> {
        override val dataClass = KingdomData::class
        override val cache = mutableMapOf<UUID, KingdomData>()

        init {
            load()
        }
    }
}
