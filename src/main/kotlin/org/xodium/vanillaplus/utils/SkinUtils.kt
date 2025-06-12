/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("unused")

package org.xodium.vanillaplus.utils

import com.google.gson.JsonParser
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.io.File
import java.net.URI
import java.util.*
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
        // 1. fetch skin URL from the playerProfile
        val texturesProp = playerProfile.properties
            .firstOrNull { it.name == "textures" }
            ?: throw IllegalStateException("Player has no skin texture")
        val json = JsonParser.parseString(String(Base64.getDecoder().decode(texturesProp.value))).asJsonObject
        val skinUrl = json
            .getAsJsonObject("textures")
            .getAsJsonObject("SKIN")
            .get("url")
            .asString

        // 2. load and crop
        val fullImg = ImageIO.read(URI.create(skinUrl).toURL())
        val face = fullImg.getSubimage(0, 0, 8, 8) //FIX: cropping face wrong.

        // 2a. save to file
        val skinsDir = File(instance.dataFolder, "skins").apply { mkdirs() }
        val outFile = File(skinsDir, "$uniqueId.png")
        ImageIO.write(face, "png", outFile)

        // 3. scale & build MiniMessage
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
                if (a == 0) builder.append("<color:#000000>█</color>")
                else builder.append("<color:#%02x%02x%02x>█</color>".format(r, g, b))
            }
            builder.append("\n")
        }
        return builder.toString()
    }
}