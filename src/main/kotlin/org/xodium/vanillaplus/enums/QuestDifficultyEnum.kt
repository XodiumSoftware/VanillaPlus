package org.xodium.vanillaplus.enums

import net.kyori.adventure.text.Component
import org.xodium.vanillaplus.utils.ExtUtils.mm

/**
 * Represents the difficulty levels for quests.
 * @param title The title component associated with the quest difficulty.
 * * EASY: Green title
 * * MEDIUM: Blue title
 * * HARD: Red title
 */
internal enum class QuestDifficultyEnum(
    val title: Component,
) {
    EASY("<green>Easy Quest".mm()),
    MEDIUM("<blue>Medium Quest".mm()),
    HARD("<red>Hard Quest".mm()),
}
