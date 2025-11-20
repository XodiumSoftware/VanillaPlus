package org.xodium.vanillaplus.utils

import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/** Formatting utilities. */
internal object FmtUtils {
    val fire = Placeholder.styling("fire", TextColor.fromHexString("<gradient:#CB2D3E:#EF473A>"))
    val mango = Placeholder.styling("mango", TextColor.fromHexString("<gradient:#FFE259:#FFA751>"))
    val birdflop = Placeholder.styling("birdflop", TextColor.fromHexString("<gradient:#54DAf4:#545EB6>"))
    val skyline = Placeholder.styling("skyline", TextColor.fromHexString("<gradient:#1488CC:#2B32B2>"))
    val rose = Placeholder.styling("rose", TextColor.fromHexString("<gradient:#F4C4F3:#FC67FA>"))
    val dawn = Placeholder.styling("dawn", TextColor.fromHexString("<gradient:#F3904F:#3B4371>"))
    val glorp = Placeholder.styling("glorp", TextColor.fromHexString("<gradient:#B3E94A:#54F47F>"))
    val spellbite = Placeholder.styling("spellbite", TextColor.fromHexString("<gradient:#832466:#BF4299:#832466>"))

    /**
     * Get all resolvers as a TagResolver array.
     * @return An array of all TagResolvers.
     */
    fun getAll(): Array<TagResolver> = arrayOf(fire, mango, birdflop, skyline, rose, dawn, glorp, spellbite)
}
