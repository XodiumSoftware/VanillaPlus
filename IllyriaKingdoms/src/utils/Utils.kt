package org.xodium.illyriaplus.utils

import net.kyori.adventure.text.minimessage.MiniMessage
import org.xodium.illyriaplus.IllyriaKingdoms

/** General utilities. */
internal object Utils {
    /** MiniMessage instance for parsing formatted strings. */
    val MM: MiniMessage = MiniMessage.miniMessage()

    /** The standardized prefix for [org.xodium.illyriaplus.IllyriaKingdoms] messages. */
    val IllyriaKingdoms.prefix: String
        get() =
            "<gradient:#FFA751:#FFE259>[</gradient><gradient:#CB2D3E:#EF473A>" +
                "${this.javaClass.simpleName}" +
                "</gradient><gradient:#FFE259:#FFA751>]</gradient>"
}
