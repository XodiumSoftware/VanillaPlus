/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils

/** Formatting utilities */
object FmtUtils {
    /** Applies a fire-themed gradient to the string. */
    fun String.fireFmt(inverted: Boolean = false): String =
        "<gradient:${if (inverted) "#EF473A:#CB2D3E" else "#CB2D3E:#EF473A"}>$this<reset>"

    /** Applies a mango-themed gradient to the string. */
    fun String.mangoFmt(inverted: Boolean = false): String =
        "<gradient:${if (inverted) "#FFA751:#FFE259" else "#FFE259:#FFA751"}>$this<reset>"
}