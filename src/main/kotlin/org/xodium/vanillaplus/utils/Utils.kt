@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.utils

import io.papermc.paper.registry.TypedKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
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

    /** Extension function to convert snake_case to Proper Case with spaces. */
    fun String.snakeToProperCase(): String =
        split('_').joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

    /** Extension function specifically for enchantment keys */
    fun TypedKey<Enchantment>.displayName(): Component = MM.deserialize(value().snakeToProperCase())
}
