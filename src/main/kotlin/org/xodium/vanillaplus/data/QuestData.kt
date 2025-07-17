package org.xodium.vanillaplus.data

import java.util.*

/**
 * Represents the data structure for a quest in the game.
 * @property id The unique identifier for the quest.
 * @property task The description of the quest goal or task.
 * @property reward The reward given upon completing the quest.
 * @property completed Indicates whether the quest has been completed. Defaults to false.
 */
data class QuestData(
    val id: UUID = UUID.randomUUID(),
    val task: String,
    val reward: String,
    val completed: Boolean = false,
)
