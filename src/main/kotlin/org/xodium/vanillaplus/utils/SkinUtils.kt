/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("unused")

package org.xodium.vanillaplus.utils

import org.bukkit.entity.Player
import java.awt.image.BufferedImage
import java.net.URI
import javax.imageio.ImageIO

//FIX: rectangles not filling the whole face (they seem rounded)
//FIX: the image not rendering properly

/** Skin utilities. */
object SkinUtils {
    /**
     * Retrieves the player's face as a MiniMessage string.
     * @param size The size of the face in pixels (default is 8).
     * @return A MiniMessage string representing the player's face.
     */
    fun Player.faceToMM(size: Int = 8): String {
        val url = URI("https://crafatar.com/avatars/$uniqueId").toURL()
        val img: BufferedImage = ImageIO.read(url)
        val face = img.getSubimage(8, 8, 8, 8)
        val scale = 8.0 / size
        val builder = StringBuilder()
        for (y in 0 until size) {
            for (x in 0 until size) {
                val px = (x * scale).toInt().coerceIn(0, 7)
                val py = (y * scale).toInt().coerceIn(0, 7)
                val rgb = face.getRGB(px, py)
                val a = (rgb ushr 24) and 0xFF
                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = rgb and 0xFF
                if (a == 0) {
                    builder.append("<color:#000000>█</color>")
                } else {
                    builder.append("<color:#%02x%02x%02x>█</color>".format(r, g, b))
                }
            }
            builder.append("\n")
        }
        return builder.toString()
    }
}