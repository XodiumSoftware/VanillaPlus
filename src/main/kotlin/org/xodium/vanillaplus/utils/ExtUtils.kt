@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.utils

import io.papermc.paper.registry.TypedKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.enchantments.Enchantment
import org.xodium.vanillaplus.VanillaPlus

/** Extension utilities. */
internal object ExtUtils {
    private val MM: MiniMessage = MiniMessage.miniMessage()

    /** The standardized prefix for [VanillaPlus] messages. */
    val VanillaPlus.prefix: String
        get() =
            "<gradient:#FFA751:#FFE259>[</gradient><gradient:#CB2D3E:#EF473A>" +
                "${this.javaClass.simpleName}" +
                "</gradient><gradient:#FFE259:#FFA751>]</gradient>"

    /**
     * Deserializes a [MiniMessage] [String] into a [Component].
     * @param resolvers Optional tag resolvers for custom tags.
     * @return The deserialized [Component].
     */
    fun String.mm(vararg resolvers: TagResolver): Component =
        if (resolvers.isEmpty()) {
            MM.deserialize(this)
        } else {
            MM.deserialize(this, TagResolver.resolver(*resolvers))
        }

    /**
     * Deserializes an iterable collection of [MiniMessage] strings into a list of Components.
     * @param resolvers Optional tag resolvers for custom tags.
     * @return The list of deserialized Components.
     */
    @JvmName("mmStringIterable")
    fun Iterable<String>.mm(vararg resolvers: TagResolver): List<Component> = map { it.mm(*resolvers) }

    /** Extension function to convert snake_case to Proper Case with spaces. */
    fun String.snakeToProperCase(): String =
        split('_').joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

    /** Extension function specifically for enchantment keys */
    fun TypedKey<Enchantment>.displayName(): Component = value().snakeToProperCase().mm()
}
