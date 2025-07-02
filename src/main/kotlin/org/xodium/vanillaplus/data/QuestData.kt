/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.xodium.vanillaplus.enums.QuestDifficulty
import java.util.*

/**
 * Represents a quest with its associated data.
 * @param difficulty The difficulty level of the quest.
 * @param task The task description of the quest.
 * @param reward The reward for completing the quest.
 * @param completed Indicates whether the quest has been completed.
 * @param claimed Indicates whether the reward for the quest has been claimed.
 * @param progress The current progress towards completing the quest.
 * @param uuid A unique identifier for the quest.
 */
data class QuestData(
    val difficulty: QuestDifficulty,
    val task: String,
    val reward: String,
    var completed: Boolean = false,
    var claimed: Boolean = false,
    var progress: Int = 0,
    val uuid: UUID = UUID.randomUUID(),
)
