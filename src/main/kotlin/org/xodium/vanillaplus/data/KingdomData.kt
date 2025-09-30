@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.data

import org.xodium.vanillaplus.interfaces.DataInterface
import org.xodium.vanillaplus.interfaces.KingdomTypeInterface
import java.util.*

/**
 * Represents a kingdom in the VanillaPlus plugin.
 * @property id The unique identifier of the kingdom.
 * @property name The display name of the kingdom.
 * @property sceptre The UUID of the sceptre who rules this kingdom.
 * @property ruler The UUID of the ruler of this kingdom.
 * @property type The type of government system for this kingdom.
 * @property creationDate The date when the kingdom was created.
 * @property members Mutable set of UUIDs representing kingdom members (excluding the ruler).
 */
internal data class KingdomData(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val sceptre: UUID,
    val ruler: UUID,
    val type: KingdomTypeInterface? = null,
    val creationDate: Date = Date(),
    val members: MutableSet<UUID> = mutableSetOf(),
    val npcs: MutableSet<UUID> = mutableSetOf(),
) {
    companion object : DataInterface<KingdomData> {
        override val dataClass = KingdomData::class
        override val cache = mutableMapOf<UUID, KingdomData>()

        init {
            load()
        }
    }
}
