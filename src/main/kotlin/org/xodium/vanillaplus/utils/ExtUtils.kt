@file:Suppress("UnstableApiUsage", "ktlint:standard:no-wildcard-imports", "unused")

package org.xodium.vanillaplus.utils

import com.google.gson.JsonParser
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Color
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.ExtUtils.lore
import org.xodium.vanillaplus.utils.ExtUtils.name
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import java.net.URI
import java.util.*
import javax.imageio.ImageIO

/** Extension utilities. */
internal object ExtUtils {
    private val MM: MiniMessage = MiniMessage.miniMessage()

    private const val DEFAULT_FACE_SIZE = 8
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
    private const val MILLISECONDS_PER_TICK = 50L

    val VanillaPlus.prefix: String
        get() = "${"[".mangoFmt(true)}${this::class.simpleName.toString().fireFmt()}${"]".mangoFmt()}"

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
     * Deserializes a list of [MiniMessage] strings into a list of Components.
     * @param resolvers Optional tag resolvers for custom tags.
     * @return The list of deserialized Components.
     */
    @JvmName("mmStringList")
    fun List<String>.mm(vararg resolvers: TagResolver): List<Component> = this.map { it.mm(*resolvers) }

    /** Serializes a [Component] into a String. */
    fun Component.mm(): String = MM.serialize(this)

    /** Deserializes a list of [MiniMessage] strings into a list of Components. */
    @JvmName("mmComponentList")
    fun List<Component>.mm(): List<String> = this.map { it.mm() }

    /** Serializes a [Component] into plaintext. */
    fun Component.pt(): String = PlainTextComponentSerializer.plainText().serialize(this)

    /** Creates an [ItemLore] object from a single [MiniMessage] [String]. */
    fun String.il(): ItemLore.Builder = ItemLore.lore().addLine(this.mm())

    /** Creates an [ItemLore] object from a list of [MiniMessage] strings. */
    @JvmName("ilStringList")
    fun List<String>.il(): ItemLore.Builder = ItemLore.lore().addLines(this.mm())

    /** Creates an [ItemLore] object from a single [Component]. */
    fun Component.il(): ItemLore.Builder = ItemLore.lore().addLine(this)

    /** Creates an [ItemLore] object from a list of Components. */
    @JvmName("ilComponentList")
    fun List<Component>.il(): ItemLore.Builder = ItemLore.lore().addLines(this)

    /** Creates a [CustomModelData] object from a single [String]. */
    fun String.cmd(): CustomModelData.Builder = CustomModelData.customModelData().addString(this)

    /** Creates a [CustomModelData] object from a list of strings. */
    @JvmName("cmdStringList")
    fun List<String>.cmd(): CustomModelData.Builder = CustomModelData.customModelData().addStrings(this)

    /** Creates a [CustomModelData] object from a single [Float]. */
    fun Float.cmd(): CustomModelData.Builder = CustomModelData.customModelData().addFloat(this)

    /** Creates a [CustomModelData] object from a list of floats. */
    @JvmName("cmdFloatList")
    fun List<Float>.cmd(): CustomModelData.Builder = CustomModelData.customModelData().addFloats(this)

    /** Creates a [CustomModelData] object from a single [Boolean]. */
    fun Boolean.cmd(): CustomModelData.Builder = CustomModelData.customModelData().addFlag(this)

    /** Creates a [CustomModelData] object from a list of booleans. */
    @JvmName("cmdBooleanList")
    fun List<Boolean>.cmd(): CustomModelData.Builder = CustomModelData.customModelData().addFlags(this)

    /** Creates a [CustomModelData] object from a single [Color]. */
    fun Color.cmd(): CustomModelData.Builder = CustomModelData.customModelData().addColor(this)

    /** Creates a [CustomModelData] object from a list of colours. */
    @JvmName("cmdColorList")
    fun List<Color>.cmd(): CustomModelData.Builder = CustomModelData.customModelData().addColors(this)

    /**
     * Performs a command from a [String].
     * @param hover Optional hover text for the command.
     * @return The formatted [String] with the command.
     */
    fun String.clickRunCmd(hover: String? = null): String =
        if (hover != null) {
            "<hover:show_text:'$hover'><click:run_command:'$this'>$this</click></hover>"
        } else {
            "<click:run_command:'$this'>$this</click>"
        }

    /**
     * Suggests a command from a [String].
     * @param hover Optional hover text for the command.
     * @return The formatted [String] with the suggested command.
     */
    fun String.clickSuggestCmd(hover: String? = null): String =
        if (hover != null) {
            "<hover:show_text:'$hover'><click:suggest_command:'$this'>$this</click></hover>"
        } else {
            "<click:suggest_command:'$this'>$this</click>"
        }

    /**
     * A helper  function to wrap command execution with standardized error handling.
     * @param action The action to execute, receiving a CommandSourceStack as a parameter.
     * @return Command.SINGLE_SUCCESS after execution.
     */
    fun CommandContext<CommandSourceStack>.tryCatch(action: (CommandSourceStack) -> Unit): Int {
        runCatching { action(this.source) }
            .onFailure { e ->
                instance.logger.severe(
                    """
                    Command error: ${e.message}
                    ${e.stackTraceToString()}
                    """.trimIndent(),
                )
                (this.source.sender as? Player)?.sendMessage(
                    "${instance.prefix} <red>An error has occurred. Check server logs for details.".mm(),
                )
            }
        return Command.SINGLE_SUCCESS
    }

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
     * Converts a CamelCase string to snake_case.
     * @return the snake_case version of the string.
     */
    fun String.toSnakeCase(): String = this.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()

    /**
     * Sets a custom display name for the ItemStack.
     * @param name the text to be set as the custom name for this ItemStack.
     * @return the modified ItemStack instance (`this`) with the new name applied.
     * @see DataComponentTypes.CUSTOM_NAME
     */
    fun ItemStack.name(name: String): ItemStack = apply { setData(DataComponentTypes.CUSTOM_NAME, name.mm()) }

    /**
     * Sets custom lore for the ItemStack.
     * @param lore the ItemLore to be set as the custom lore for this ItemStack.
     * @return the modified ItemStack instance (`this`) with the new lore applied.
     * @see DataComponentTypes.LORE
     */
    fun ItemStack.lore(lore: List<String>): ItemStack = apply { setData(DataComponentTypes.LORE, ItemLore.lore(lore.mm())) }
}
