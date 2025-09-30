package org.xodium.vanillaplus.enums

import net.kyori.adventure.text.Component
import org.xodium.vanillaplus.utils.ExtUtils.mm

/** Enum for kingdom government types. */
internal enum class KingdomTypeEnum {
    FEUDALISM,
    AUTOCRACY,
    REPUBLIC,
    THEOCRACY,
    ;

    /**
     * Returns the MiniMessage color code for this kingdom type.
     * @return The MiniMessage color code string for this kingdom type
     */
    private fun getColorCode(): String =
        when (this) {
            FEUDALISM -> "<dark_aqua>"
            AUTOCRACY -> "<red>"
            REPUBLIC -> "<green>"
            THEOCRACY -> "<gold>"
        }

    /** Returns the enum value formatted with only the first letter capitalized. */
    fun displayName(): Component = "<b>${getColorCode()}${name.lowercase().replaceFirstChar { it.uppercase() }}</b>".mm()
}
