@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.illyriaplus.utils

import com.google.gson.JsonParser
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Chunk
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.IllyriaCore.Companion.instance
import org.xodium.illyriaplus.pdcs.PlayerPDC.nickname
import org.xodium.illyriaplus.pdcs.PlayerPDC.scoreboardVisibility
import org.xodium.illyriaplus.utils.Utils.MM
import java.net.URI
import javax.imageio.ImageIO
import kotlin.io.encoding.Base64

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
     * Retrieves a [Player]'s face as a [String].
     *
     * @param size The size of the face in pixels (default is 8).
     * @return A [String] representing the player's face.
     */
    fun Player.face(size: Int = 8): String {
        // 1. fetch skin URL from the playerProfile
        val texturesProp =
            playerProfile.properties
                .firstOrNull { it.name == "textures" }
                ?: error("Player has no skin texture")
        val json = JsonParser.parseString(Base64.decode(texturesProp.value).decodeToString()).asJsonObject
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

        return buildString {
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
                        append("<color:$BLACK_COLOR>$PIXEL_CHAR</color>")
                    } else {
                        append("<color:#%02x%02x%02x>$PIXEL_CHAR</color>".format(r, g, b))
                    }
                }
                append("\n")
            }
        }
    }

    /**
     * Get [Container]s around a [Player] (3x3 [Chunk] area).
     *
     * @return [Set] of [Container]s around the player.
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
     * Get [Chunk]s around a [Player] (3x3 [Chunk] area).
     *
     * @param range The [Chunk] radius around the player (1 = 3x3 area).
     * @return [Set] of [Chunk]s around the player.
     */
    fun Player.getChunksAround(range: Int = 1): Set<Chunk> {
        val (baseX, baseZ) = location.chunk.run { x to z }

        return buildSet {
            for (x in -range..range) {
                for (z in -range..range) {
                    add(world.getChunkAt(baseX + x, baseZ + z))
                }
            }
        }
    }

    /**
     * Gets the first leashed [Tameable] entity owned by a [Player] within the config radius.
     *
     * @param radius The radius within which to search for leashed entities.
     * @return The found [Tameable] entity or `null` if none exists.
     */
    fun Player.getLeashedEntity(radius: Double = 10.0): Tameable? =
        getNearbyEntities(radius, radius, radius)
            .filterIsInstance<Tameable>()
            .firstOrNull { it.isLeashed && it.leashHolder == this }

    /**
     * Applies the correct scoreboard to a [Player] based on their visibility preference.
     */
    fun Player.applyScoreboard() {
        scoreboard =
            if (scoreboardVisibility) {
                instance.server.scoreboardManager.newScoreboard
            } else {
                instance.server.scoreboardManager.mainScoreboard
            }
    }

    /**
     * Modifies the colour of a [Player]'s waypoint based on the specified parameters.
     *
     * @param color The optional [TextColor] to apply to the waypoint.
     */
    fun Player.locator(color: TextColor? = null) {
        waypointColor = color?.let { Color.fromRGB(it.value()) }
        sendActionBar(Component.text("Locator color changed!", color))
    }

    /** Sets the display name of the player based on their nickname. */
    fun Player.setNickname() = displayName(MM.deserialize(nickname))

    /** Returns an [ItemStack] of this player's head with their skin profile applied. */
    @Suppress("UnstableApiUsage")
    fun Player.head(): ItemStack =
        ItemStack.of(Material.PLAYER_HEAD).apply {
            setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(playerProfile))
        }
}
