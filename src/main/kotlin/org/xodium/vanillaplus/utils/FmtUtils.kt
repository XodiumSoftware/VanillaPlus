package org.xodium.vanillaplus.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/** Formatting utilities. */
internal object FmtUtils {
    private val fire = Placeholder.component("fire", Component.text("<gradient:#CB2D3E:#EF473A>"))
    private val fire_ = Placeholder.component("/fire", Component.text("</gradient>"))
    private val mango = Placeholder.component("mango", Component.text("<gradient:#FFE259:#FFA751>"))
    private val mango_ = Placeholder.component("/mango", Component.text("</gradient>"))
    private val birdflop = Placeholder.component("birdflop", Component.text("<gradient:#54DAf4:#545EB6>"))
    private val birdflop_ = Placeholder.component("/birdflop", Component.text("</gradient>"))
    private val skyline = Placeholder.component("skyline", Component.text("<gradient:#1488CC:#2B32B2>"))
    private val skyline_ = Placeholder.component("/skyline", Component.text("</gradient>"))
    private val rose = Placeholder.component("rose", Component.text("<gradient:#F4C4F3:#FC67FA>"))
    private val rose_ = Placeholder.component("/rose", Component.text("</gradient>"))
    private val dawn = Placeholder.component("dawn", Component.text("<gradient:#F3904F:#3B4371>"))
    private val dawn_ = Placeholder.component("/dawn", Component.text("</gradient>"))
    private val glorp = Placeholder.component("glorp", Component.text("<gradient:#B3E94A:#54F47F>"))
    private val glorp_ = Placeholder.component("/glorp", Component.text("</gradient>"))
    private val spellbite = Placeholder.component("spellbite", Component.text("<gradient:#832466:#BF4299:#832466>"))
    private val spellbite_ = Placeholder.component("/spellbite", Component.text("</gradient>"))

    /**
     * Get all resolvers as a TagResolver array.
     * @return An array of all TagResolvers.
     */
    fun getAll(): Array<TagResolver> =
        arrayOf(
            fire,
            fire_,
            mango,
            mango_,
            birdflop,
            birdflop_,
            skyline,
            skyline_,
            rose,
            rose_,
            dawn,
            dawn_,
            glorp,
            glorp_,
            spellbite,
            spellbite_,
        )
}
