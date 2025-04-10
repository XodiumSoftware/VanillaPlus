/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * Formatting utilities
 */
object FmtUtils {
    private val MM: MiniMessage = MiniMessage.miniMessage()

    fun String.mm(): Component = MM.deserialize(this)
    fun List<String>.mm(): List<Component> = this.map { it.mm() }
    fun String.fireFmt(inverted: Boolean = false): String =
        "<gradient:${if (inverted) "#EF473A:#CB2D3E" else "#CB2D3E:#EF473A"}>$this<reset>"

    fun String.mangoFmt(inverted: Boolean = false): String =
        "<gradient:${if (inverted) "#FFA751:#FFE259" else "#FFE259:#FFA751"}>$this<reset>"
}