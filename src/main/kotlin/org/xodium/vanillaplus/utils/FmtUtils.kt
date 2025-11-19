@file:Suppress("unused")

package org.xodium.vanillaplus.utils

/** Formatting utilities. */
internal object FmtUtils {
    /**
     * Applies a gradient to the given text using the specified colours.
     * @param colors The colours to use in the gradient (minimum 2 required).
     * @param inverted Whether to invert the gradient colours.
     * @return The formatted [String] with the gradient applied.
     */
    private fun String.gradient(
        vararg colors: String,
        inverted: Boolean,
    ): String {
        require(colors.size >= 2) { "At least 2 colors are required for a gradient." }
        val gradientColors = if (inverted) colors.reversed().toTypedArray() else colors
        val gradientTags = gradientColors.joinToString(":")

        return "<gradient:$gradientTags>$this<reset>"
    }

    /** Applies a fire-themed gradient to the [String]. */
    fun String.fireFmt(inverted: Boolean = false): String = gradient("#CB2D3E", "#EF473A", inverted = inverted)

    /** Applies a mango-themed gradient to the [String]. */
    fun String.mangoFmt(inverted: Boolean = false): String = gradient("#FFE259", "#FFA751", inverted = inverted)

    /** Applies a birdflop-themed gradient to the [String]. */
    fun String.birdflopFmt(inverted: Boolean = false): String = gradient("#54DAf4", "#545EB6", inverted = inverted)

    /** Applies a skyline-themed gradient to the [String]. */
    fun String.skylineFmt(inverted: Boolean = false): String = gradient("#1488CC", "#2B32B2", inverted = inverted)

    /** Applies a rose-themed gradient to the [String]. */
    fun String.roseFmt(inverted: Boolean = false): String = gradient("#F4C4F3", "#FC67FA", inverted = inverted)

    /** Applies a dawn-themed gradient to the [String]. */
    fun String.dawnFmt(inverted: Boolean = false): String = gradient("#F3904F", "#3B4371", inverted = inverted)

    /** Applies a glorp-themed gradient to the [String]. */
    fun String.glorpFmt(inverted: Boolean = false): String = gradient("#B3E94A", "#54F47F", inverted = inverted)

    /** Applies a spellbite-themed gradient to the [String]. */
    fun String.spellbiteFmt(inverted: Boolean = false): String = gradient("#832466", "#BF4299", "#832466", inverted = inverted)
}
