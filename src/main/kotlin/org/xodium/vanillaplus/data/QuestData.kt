/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.xodium.vanillaplus.enums.QuestDifficulty

/**
 * Represents a quest with its associated data.
 * @param difficulty The difficulty level of the quest.
 * @param task The task description of the quest.
 * @param reward The reward for completing the quest.
 * @param completed Indicates whether the quest has been completed.
 */
data class QuestData(
    val difficulty: QuestDifficulty,
    val task: String,
    val reward: String,
    var completed: Boolean = false
)
