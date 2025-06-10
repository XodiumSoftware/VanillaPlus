/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import net.kyori.adventure.title.Title
import org.xodium.vanillaplus.utils.ExtUtils.mm

/**
 * Represents the title data used in the system.
 * @property title The main title text.
 * @property subtitle The subtitle text.
 */
data class TitleData(
    private val title: String,
    private val subtitle: String,
) {
    fun toTitle(): Title = Title.title(title.mm(), subtitle.mm())
}
