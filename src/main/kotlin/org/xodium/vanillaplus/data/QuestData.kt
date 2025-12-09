@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.data

import org.xodium.vanillaplus.enums.QuestTypeEnum
import java.util.*

/** Data class representing a quest with its objective and rewards. */
internal data class QuestData(
    val uuid: UUID = UUID.randomUUID(),
    val objective: String,
    val crystals: Int,
    val type: QuestTypeEnum,
    val requiredAmount: Int,
)
