package org.xodium.illyriaplus.data

import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import org.xodium.illyriaplus.Utils.MM
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Represents a kingdom in the IllyriaKingdoms plugin.
 *
 * @property id Unique identifier for the kingdom.
 * @property name Display name of the kingdom (MiniMessage format supported).
 * @property owner UUID of the kingdom owner.
 * @property members List of member UUIDs belonging to the kingdom.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
internal data class KingdomData(
    val id: Uuid = Uuid.random(),
    val name: String,
    val owner: Uuid,
    val members: List<Uuid>,
) {
    /**
     * Deserializes the kingdom name into a [Component] for display.
     *
     * @return The deserialized [Component] representation of the kingdom name.
     */
    fun displayName(): Component = MM.deserialize(name)

    /**
     * Serializes a [Component] into a string format.
     *
     * @param name The [Component] to serialize.
     * @return The serialized string representation of the component.
     */
    fun displayName(name: Component) = MM.serialize(name)
}
