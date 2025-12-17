package org.xodium.vanillaplus.data

import kotlinx.serialization.Serializable

/** Represents configuration for an army of monsters. */
@Serializable
internal data class ArmyData(
    val name: String,
    val armySize: Int = 50,
    val spawnRadius: Double = 20.0,
    val spawnDelay: Int = 10,
    val monsters: List<MonsterData>,
)
