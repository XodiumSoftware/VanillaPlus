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
import org.bukkit.Color

//TODO: FIX.

/** Extension utilities */
object ExtUtils {
    private val MM: MiniMessage = MiniMessage.miniMessage()
    private val IL: ItemLore.Builder = ItemLore.lore()
    private val CMD: CustomModelData.Builder = CustomModelData.customModelData()

    /** Deserializes a MiniMessage string into a Component. */
    fun String.mm(): Component = MM.deserialize(this)

    /** Deserializes a list of MiniMessage strings into a list of Components. */
    fun List<String>.mm(): List<Component> = this.map { it.mm() }

    /** Serializes a Component into a MiniMessage string. */
    fun Component.mm(): String = MM.serialize(this)

    /** Deserializes a list of MiniMessage strings into a list of Components. */
//    fun List<Component>.mm(): List<String> = this.map { it.mm() }

    /** Creates an ItemLore object from a single MiniMessage string. */
    fun String.il(): ItemLore.Builder = IL.addLine(this.mm())

    /** Creates an ItemLore object from a list of MiniMessage strings. */
    fun List<String>.il(): ItemLore.Builder = IL.addLines(this.mm())

    /** Creates an ItemLore object from a single Component. */
    fun Component.il(): ItemLore.Builder = IL.addLine(this)

    /** Creates an ItemLore object from a list of Components. */
//    fun List<Component>.il(): ItemLore.Builder = IL.addLines(this)

    /** Creates a CustomModelData object from a single string. */
    fun String.cmd(): CustomModelData.Builder = CMD.addString(this)

    /** Creates a CustomModelData object from a list of strings. */
    fun List<String>.cmd(): CustomModelData.Builder = CMD.addStrings(this)

    /** Creates a CustomModelData object from a single float. */
    fun Float.cmd(): CustomModelData.Builder = CMD.addFloat(this)

    /** Creates a CustomModelData object from a list of floats. */
//    fun List<Float>.cmd(): CustomModelData.Builder = CMD.addFloats(this)

    /** Creates a CustomModelData object from a single boolean. */
    fun Boolean.cmd(): CustomModelData.Builder = CMD.addFlag(this)

    /** Creates a CustomModelData object from a list of booleans. */
//    fun List<Boolean>.cmd(): CustomModelData.Builder = CMD.addFlags(this)

    /** Creates a CustomModelData object from a single color. */
    fun Color.cmd(): CustomModelData.Builder = CMD.addColor(this)

    /** Creates a CustomModelData object from a list of colors. */
//    fun List<Color>.cmd(): CustomModelData.Builder = CMD.addColors(this)
}
