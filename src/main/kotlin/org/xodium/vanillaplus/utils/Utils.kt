@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.utils

import io.papermc.paper.registry.TypedKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.enchantments.Enchantment
import org.xodium.vanillaplus.VanillaPlus
import java.util.*

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

    /** Extension function to convert a UUID to a 4-element integer array representation. */
    fun UUID.toIntArray(): IntArray {
        val most = mostSignificantBits
        val least = leastSignificantBits
        return intArrayOf((most shr 32).toInt(), most.toInt(), (least shr 32).toInt(), least.toInt())
    }
}
