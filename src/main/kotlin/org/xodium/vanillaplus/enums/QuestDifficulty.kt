/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.enums

import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.glorpFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt

/**
 * Represents the difficulty levels of quests in the system.
 * @property title The formatted title of the quest difficulty.
 */
enum class QuestDifficulty(val title: String) {
    EASY("<b>Easy</b>".glorpFmt()),
    MEDIUM("<b>Medium</b>".mangoFmt()),
    HARD("<b>Hard</b>".fireFmt())
}