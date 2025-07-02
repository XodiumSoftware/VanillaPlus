/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

/**
 * A wrapper for a player's quests and their generation timestamp.
 * @param list The list of [QuestData].
 * @param timestamp The epoch milliseconds when the quests were generated.
 */
data class PlayerQuestsData(
    val list: List<QuestData> = emptyList(),
    val timestamp: Long = 0L,
)
