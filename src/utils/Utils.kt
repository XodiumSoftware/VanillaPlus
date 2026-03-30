@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.utils

import io.papermc.paper.registry.TypedKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.World
import org.bukkit.enchantments.Enchantment
import org.xodium.vanillaplus.VanillaPlus

/** General utilities. */
internal object Utils {
    /** MiniMessage instance for parsing formatted strings. */
    val MM: MiniMessage = MiniMessage.miniMessage()

    /** The standardized prefix for [VanillaPlus] messages. */
    val VanillaPlus.prefix: String
        get() =
            "<gradient:#FFA751:#FFE259>[</gradient><gradient:#CB2D3E:#EF473A>" +
                "${this.javaClass.simpleName}" +
                "</gradient><gradient:#FFE259:#FFA751>]</gradient>"

    /** Extension function to convert a positive [Int] to its Roman numeral string representation. */
    fun Int.toRoman(): String {
        val numerals =
            listOf(
                1000 to "M",
                900 to "CM",
                500 to "D",
                400 to "CD",
                100 to "C",
                90 to "XC",
                50 to "L",
                40 to "XL",
                10 to "X",
                9 to "IX",
                5 to "V",
                4 to "IV",
                1 to "I",
            )

        var num = this

        return buildString {
            for ((value, symbol) in numerals) {
                repeat(num / value) { append(symbol) }
                num %= value
            }
        }
    }

    /** Extension function to convert snake_case to Proper Case with spaces. */
    fun String.snakeToProperCase(): String =
        split('_').joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

    /** Extension function specifically for enchantment keys */
    fun TypedKey<Enchantment>.displayName(): Component = MM.deserialize(value().snakeToProperCase())

    /** Extension function to convert CamelCase to snake_case, removing a specified suffix. */
    inline fun <reified T> Class<*>.toRegistryKeyFragment(): String =
        simpleName
            .removeSuffix(T::class.simpleName ?: "")
            .split(Regex("(?=[A-Z])"))
            .filter { it.isNotEmpty() }
            .joinToString("_") { it.lowercase() }

    /**
     * Returns the i18n string matching the current weather state of this world.
     * @param thundering The string to return when it is thundering.
     * @param storm The string to return when there is a storm.
     * @param clear The string to return when the weather is clear.
     */
    fun World.weather(
        thundering: String,
        storm: String,
        clear: String,
    ): String =
        when {
            isThundering -> thundering
            hasStorm() -> storm
            else -> clear
        }
}
