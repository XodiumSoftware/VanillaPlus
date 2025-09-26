@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.data

import org.xodium.vanillaplus.interfaces.DataInterface
import java.util.*

/**
 * Represents a kingdom in the VanillaPlus plugin.
 * @property id The unique identifier of the kingdom.
 * @property name The display name of the kingdom.
 * @property sceptre The UUID of the sceptre who rules this kingdom.
 * @property creationDate The date when the kingdom was created.
 * @property members Mutable set of UUIDs representing kingdom members (excluding the ruler).
 * //TODO
 */
data class KingdomData(
    val id: UUID,
    val name: String,
    val sceptre: UUID,
    val ruler: UUID,
    val creationDate: Date,
    val members: MutableSet<UUID> = mutableSetOf(),
    val npcs: MutableSet<UUID> = mutableSetOf(),
) {
    companion object : DataInterface<KingdomData> {
        override val dataClass = KingdomData::class
        override val cache = mutableMapOf<UUID, KingdomData>()

        init {
            load()
            cache.values.forEach { kingdom ->
                sceptreToKingdom[kingdom.sceptre] = kingdom.id
            }
        }

        fun getKingdomBySceptre(sceptreId: UUID): KingdomData? = sceptreToKingdom[sceptreId]?.let { cache[it] }

        fun createNewKingdom(
            sceptreId: UUID,
            sceptreHolder: UUID,
            kingdomName: String,
        ): KingdomData {
            val kingdom =
                KingdomData(
                    id = UUID.randomUUID(),
                    name = kingdomName,
                    sceptre = sceptreId,
                    sceptreHolder = sceptreHolder,
                    creationDate = Date(),
                )

            cache[kingdom.id] = kingdom
            sceptreToKingdom[sceptreId] = kingdom.id
            save()

            return kingdom
        }
    }
}
