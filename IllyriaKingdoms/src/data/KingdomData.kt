package org.xodium.illyriaplus.data

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import org.xodium.illyriaplus.utils.Utils.MM
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents a kingdom in the IllyriaKingdoms plugin.
 * Ownership is dynamic - whoever holds the kingdom's sceptre is the owner.
 *
 * @property id Unique identifier for the kingdom.
 * @property name Display name of the kingdom (MiniMessage format supported).
 * @property members List of member UUIDs belonging to the kingdom.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
internal data class KingdomData(
    val id: Uuid = Uuid.random(),
    val name: String,
    val members: List<Uuid>,
) {
    /**
     * Deserializes the kingdom name into a [Component] for display.
     *
     * @return The deserialized [Component] representation of the kingdom name.
     */
    fun displayName(): Component = MM.deserialize(name)

    /**
     * Returns a copy of this [KingdomData] with the name set from a [Component].
     *
     * @param name The [Component] to serialize and set as the kingdom name.
     * @return A new [KingdomData] instance with the updated name.
     */
    fun displayName(name: Component): KingdomData = copy(name = MM.serialize(name))
}
