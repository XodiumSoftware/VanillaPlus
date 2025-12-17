package org.xodium.vanillaplus.data

import kotlinx.serialization.Serializable
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.xodium.vanillaplus.serializers.AttributeSerializer

typealias AttributeRangeMap = Map<
    @Serializable(with = AttributeSerializer::class)
    Attribute,
    Pair<Double, Double>,
>

/** Represents the data structure for monster configuration. */
@Serializable
internal data class MonsterData(
    val entityType: EntityType,
    val spawnModifier: Int,
    val attributes: AttributeRangeMap,
    val displayNames: Collection<String> = emptyList(),
)
