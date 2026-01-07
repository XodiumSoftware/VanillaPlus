@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.utils

import com.google.gson.JsonParser
import org.bukkit.Chunk
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import java.net.URI
import java.util.*
import javax.imageio.ImageIO

/** Player utilities. */
internal object PlayerUtils {
    private const val FACE_X = 8
    private const val FACE_Y = 8
    private const val FACE_WIDTH = 8
    private const val FACE_HEIGHT = 8
    private const val MAX_COORDINATE = 7
    private const val COLOR_MASK = 0xFF
    private const val BLACK_COLOR = "#000000"
    private const val PIXEL_CHAR = "█"
    private const val ALPHA_SHIFT = 24
    private const val RED_SHIFT = 16
    private const val GREEN_SHIFT = 8

    /**
     * Retrieves the player's face as a string.
     * @param size The size of the face in pixels (default is 8).
     * @return A string representing the player's face.
     */
    fun Player.face(size: Int = 8): String {
        // 1. fetch skin URL from the playerProfile
        val texturesProp =
            playerProfile.properties
                .firstOrNull { it.name == "textures" }
                ?: error("Player has no skin texture")
        val json = JsonParser.parseString(String(Base64.getDecoder().decode(texturesProp.value))).asJsonObject
        val skinUrl =
            json
                .getAsJsonObject("textures")
                .getAsJsonObject("SKIN")
                .get("url")
                .asString

        // 2. load and crop
        val fullImg = ImageIO.read(URI.create(skinUrl).toURL()) ?: error("Failed to load skin image from URL: $skinUrl")
        val face = fullImg.getSubimage(FACE_X, FACE_Y, FACE_WIDTH, FACE_HEIGHT)

        // 3. scale & build MiniMessage
        val scale = FACE_WIDTH.toDouble() / size
        val builder = StringBuilder()

        for (y in 0 until size) {
            for (x in 0 until size) {
                val px = (x * scale).toInt().coerceAtMost(MAX_COORDINATE)
                val py = (y * scale).toInt().coerceAtMost(MAX_COORDINATE)
                val rgb = face.getRGB(px, py)
                val a = (rgb ushr ALPHA_SHIFT) and COLOR_MASK
                val r = (rgb shr RED_SHIFT) and COLOR_MASK
                val g = (rgb shr GREEN_SHIFT) and COLOR_MASK
                val b = rgb and COLOR_MASK

                if (a == 0) {
                    builder.append("<color:$BLACK_COLOR>$PIXEL_CHAR</color>")
                } else {
                    builder.append("<color:#%02x%02x%02x>$PIXEL_CHAR</color>".format(r, g, b))
                }
            }
            builder.append("\n")
        }
        return builder.toString()
    }

    /**
     * Get containers around this player (3x3 chunk area).
     * @receiver The player.
     * @return Collection of containers around the player.
     */
    fun Player.getContainersAround(): Set<Container> =
        buildSet {
            for (chunk in getChunksAround()) {
                for (state in chunk.tileEntities) {
                    if (state is Container) add(state)
                }
            }
        }

    /**
     * Get chunks around this player (3x3 chunk area).
     * @receiver The player.
     * @return Collection of chunks around the player.
     */
    fun Player.getChunksAround(): Set<Chunk> {
        val (baseX, baseZ) = location.chunk.run { x to z }

        return buildSet {
            for (x in -1..1) {
                for (z in -1..1) {
                    add(world.getChunkAt(baseX + x, baseZ + z))
                }
            }
        }
    }

    /**
     * Gets the first leashed entity owned by the player within the config radius.
     * @receiver The player whose leashed entity is to be found.
     * @param radius The radius within which to search for leashed entities.
     * @return The found tameable entity or `null` if none exists.
     */
    fun Player.getLeashedEntity(radius: Double = 10.0): Tameable? =
        getNearbyEntities(radius, radius, radius)
            .filterIsInstance<Tameable>()
            .firstOrNull { it.isLeashed && it.leashHolder == player }
}
