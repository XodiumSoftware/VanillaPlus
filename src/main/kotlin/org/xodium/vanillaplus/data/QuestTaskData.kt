/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import java.util.*

data class QuestTaskData(
    val uuid: UUID = UUID.randomUUID(),
    val action: String,
    val target: Any,
    val amount: Int,
)