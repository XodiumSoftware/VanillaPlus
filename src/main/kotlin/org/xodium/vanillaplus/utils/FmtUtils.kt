@file:Suppress("unused")

package org.xodium.vanillaplus.utils

/** Formatting utilities. */
internal object FmtUtils {
    /**
     * Applies a gradient to the given text using the specified colours.
     * @param color1 The starting colour of the gradient.
     * @param color2 The ending colour of the gradient.
     * @param text The text to apply the gradient to.
     * @param inverted Whether to invert the gradient colours.
     * @return The formatted [String] with the gradient applied.
     */
    private fun gradient(color1: String, color2: String, text: String, inverted: Boolean): String {
        val startColor = if (inverted) color2 else color1
        val endColor = if (inverted) color1 else color2
        return "<gradient:$startColor:$endColor>$text<reset>"
    }

    /** Applies a fire-themed gradient to the [String]. */
    fun String.fireFmt(inverted: Boolean = false): String =
        gradient("#CB2D3E", "#EF473A", this, inverted)

    /** Applies a mango-themed gradient to the [String]. */
    fun String.mangoFmt(inverted: Boolean = false): String =
        gradient("#FFE259", "#FFA751", this, inverted)

    /** Applies a birdflop-themed gradient to the [String]. */
    fun String.birdflopFmt(inverted: Boolean = false): String =
        gradient("#54DAf4", "#545EB6", this, inverted)

    /** Applies a skyline-themed gradient to the [String]. */
    fun String.skylineFmt(inverted: Boolean = false): String =
        gradient("#1488CC", "#2B32B2", this, inverted)

    /** Applies a rose-themed gradient to the [String]. */
    fun String.roseFmt(inverted: Boolean = false): String =
        gradient("#F4C4F3", "#FC67FA", this, inverted)

    /** Applies a dawn-themed gradient to the [String]. */
    fun String.dawnFmt(inverted: Boolean = false): String =
        gradient("#F3904F", "#3B4371", this, inverted)

    /** Applies a glorp-themed gradient to the [String]. */
    fun String.glorpFmt(inverted: Boolean = false): String =
        gradient("#B3E94A", "#54F47F", this, inverted)
}