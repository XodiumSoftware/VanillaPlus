@file:Suppress("unused", "UnstableApiUsage")

package org.xodium.vanillaplus.utils

import com.google.gson.JsonParser
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Color
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus.Companion.PREFIX
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.net.URI
import java.util.*
import javax.imageio.ImageIO

/** Extension utilities. */
object ExtUtils {
    private val MM: MiniMessage = MiniMessage.miniMessage()

    /**
     * Deserializes a [MiniMessage] [String] into a [Component].
     * @param resolvers Optional tag resolvers for custom tags.
     * @return The deserialized [Component].
     */
    fun String.mm(vararg resolvers: TagResolver): Component =
        if (resolvers.isEmpty()) MM.deserialize(this)
        else MM.deserialize(this, TagResolver.resolver(*resolvers))

    /**
     * Deserializes a list of [MiniMessage] strings into a list of Components.
     * @param resolvers Optional tag resolvers for custom tags.
     * @return The list of deserialized Components.
     */
    @JvmName("mmStringList")
    fun List<String>.mm(vararg resolvers: TagResolver): List<Component> =
        this.map { it.mm(*resolvers) }

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

    /** Creates a [CustomModelData] object from a list of colors. */
    @JvmName("cmdColorList")
    fun List<Color>.cmd(): CustomModelData.Builder = CustomModelData.customModelData().addColors(this)

    /**
     * Performs a command from a [String].
     * @param hover Optional hover text for the command.
     * @return The formatted [String] with the command.
     */
    fun String.clickRunCmd(hover: String? = null): String {
        return if (hover != null) {
            "<hover:show_text:'$hover'><click:run_command:'$this'>$this</click></hover>"
        } else {
            "<click:run_command:'$this'>$this</click>"
        }
    }

    /**
     * Suggests a command from a [String].
     * @param hover Optional hover text for the command.
     * @return The formatted [String] with the suggested command.
     */
    fun String.clickSuggestCmd(hover: String? = null): String {
        return if (hover != null) {
            "<hover:show_text:'$hover'><click:suggest_command:'$this'>$this</click></hover>"
        } else {
            "<click:suggest_command:'$this'>$this</click>"
        }
    }

    /**
     * A helper function to wrap command execution with standardised error handling.
     * @param action The action to execute, receiving a CommandSourceStack as a parameter.
     * @return Command.SINGLE_SUCCESS after execution.
     */
    fun CommandContext<CommandSourceStack>.tryCatch(action: (CommandSourceStack) -> Unit): Int {
        try {
            action(this.source)
        } catch (e: Exception) {
            instance.logger.severe("An Error has occurred: ${e.message}")
            e.printStackTrace()
            (this.source.sender as Player).sendMessage("$PREFIX <red>An Error has occurred. Check server logs for details.".mm())
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
        if (fullImg == null) {
            throw IllegalStateException("Failed to load skin image from URL: $skinUrl")
        }
        val face = fullImg.getSubimage(8, 8, 8, 8)

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
