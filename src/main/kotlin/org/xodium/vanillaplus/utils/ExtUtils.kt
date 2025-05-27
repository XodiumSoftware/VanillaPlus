/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("unused", "UnstableApiUsage")

package org.xodium.vanillaplus.utils

import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Color

/** Extension utilities. */
object ExtUtils {
    private val MM: MiniMessage = MiniMessage.miniMessage()
    private val IL: ItemLore.Builder = ItemLore.lore()
    private val CMD: CustomModelData.Builder = CustomModelData.customModelData()

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

    /** Serializes a [Component] into a [MiniMessage] [String]. */
    fun Component.mm(): String = MM.serialize(this)

    /** Deserializes a list of [MiniMessage] strings into a list of Components. */
    @JvmName("mmComponentList")
    fun List<Component>.mm(): List<String> = this.map { it.mm() }

    /** Creates an [ItemLore] object from a single [MiniMessage] [String]. */
    fun String.il(): ItemLore.Builder = IL.addLine(this.mm())

    /** Creates an [ItemLore] object from a list of [MiniMessage] strings. */
    @JvmName("ilStringList")
    fun List<String>.il(): ItemLore.Builder = IL.addLines(this.mm())

    /** Creates an [ItemLore] object from a single [Component]. */
    fun Component.il(): ItemLore.Builder = IL.addLine(this)

    /** Creates an [ItemLore] object from a list of Components. */
    @JvmName("ilComponentList")
    fun List<Component>.il(): ItemLore.Builder = IL.addLines(this)

    /** Creates a [CustomModelData] object from a single [String]. */
    fun String.cmd(): CustomModelData.Builder = CMD.addString(this)

    /** Creates a [CustomModelData] object from a list of strings. */
    @JvmName("cmdStringList")
    fun List<String>.cmd(): CustomModelData.Builder = CMD.addStrings(this)

    /** Creates a [CustomModelData] object from a single [Float]. */
    fun Float.cmd(): CustomModelData.Builder = CMD.addFloat(this)

    /** Creates a [CustomModelData] object from a list of floats. */
    @JvmName("cmdFloatList")
    fun List<Float>.cmd(): CustomModelData.Builder = CMD.addFloats(this)

    /** Creates a [CustomModelData] object from a single [Boolean]. */
    fun Boolean.cmd(): CustomModelData.Builder = CMD.addFlag(this)

    /** Creates a [CustomModelData] object from a list of booleans. */
    @JvmName("cmdBooleanList")
    fun List<Boolean>.cmd(): CustomModelData.Builder = CMD.addFlags(this)

    /** Creates a [CustomModelData] object from a single [Color]. */
    fun Color.cmd(): CustomModelData.Builder = CMD.addColor(this)

    /** Creates a [CustomModelData] object from a list of colors. */
    @JvmName("cmdColorList")
    fun List<Color>.cmd(): CustomModelData.Builder = CMD.addColors(this)

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
}
