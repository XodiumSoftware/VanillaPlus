/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import org.xodium.vanillaplus.enums.QuestDifficulty

/**
 * Represents a quest with its associated data.
 * @param difficulty The difficulty level of the quest.
 * @param task The task that needs to be completed for the quest.
 * @param reward The reward that will be given upon completion of the quest.
 * @param completed Indicates whether the quest has been completed.
 * @param claimed Indicates whether the reward for the quest has been claimed.
 * @param progress The current progress towards completing the quest.
 */
data class QuestData(
    val difficulty: QuestDifficulty,
    val task: QuestTaskData,
    val reward: QuestRewardData,
    var completed: Boolean = false,
    var claimed: Boolean = false,
    var progress: Int = 0,
)
