package org.xodium.vanillaplus.enums

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/** Custom MiniMessage tag resolvers for various color gradients. */
internal enum class CustomTagResolversEnum(
    val tagName: String,
    val colors: Array<String>,
) {
    FIRE("fire", arrayOf("#CB2D3E", "#EF473A")),
    MANGO("mango", arrayOf("#FFE259", "#FFA751")),
    BIRDFLOP("birdflop", arrayOf("#54DAf4", "#545EB6")),
    SKYLINE("skyline", arrayOf("#1488CC", "#2B32B2")),
    ROSE("rose", arrayOf("#F4C4F3", "#FC67FA")),
    DAWN("dawn", arrayOf("#F3904F", "#3B4371")),
    GLORP("glorp", arrayOf("#B3E94A", "#54F47F")),
    SPELLBITE("spellbite", arrayOf("#832466", "#BF4299", "#832466")),
    ;

    /**
     * Create a [TagResolver] for this gradient.
     * @param inverted Whether to invert the gradient colors.
     * @return The created [TagResolver].
     */
    fun createResolver(inverted: Boolean = false): TagResolver =
        TagResolver.resolver(if (inverted) "${tagName}_inverted" else tagName) { _, _ ->
            val gradientColors = if (inverted) colors.reversed().toTypedArray() else colors
            val colorTags = gradientColors.joinToString(":") { color -> "<color:$color>" }

            Tag.inserting(MiniMessage.miniMessage().deserialize("<gradient:$colorTags>"))
        }

    val resolver: TagResolver get() = createResolver()
    val invertedResolver: TagResolver get() = createResolver(inverted = true)

    companion object {
        /**
         * Get all resolvers from all enum values.
         * @return Array of all resolvers (both regular and inverted).
         */
        fun getAll(): Array<TagResolver> = entries.flatMap { enum -> listOf(enum.resolver, enum.invertedResolver) }.toTypedArray()
    }
}
