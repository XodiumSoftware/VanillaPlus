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

/** Represents configuration for an army of monsters. */
@Serializable
internal data class ArmyData(
    val name: String,
    val armySize: Int = 50,
    val spawnRadius: Double = 20.0,
    val spawnDelay: Int = 10,
    val monsters: List<MonsterData>,
) {
    /** Represents the data structure for monster configuration. */
    @Serializable
    internal data class MonsterData(
        val entityType: EntityType,
        val spawnModifier: Int,
        val attributes: AttributeRangeMap,
        val displayNames: Collection<String> = emptyList(),
    )
}
