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

/** Extension utilities */
object ExtUtils {
    private val MM: MiniMessage = MiniMessage.miniMessage()
    private val IL: ItemLore.Builder = ItemLore.lore()
    private val CMD: CustomModelData.Builder = CustomModelData.customModelData()

    /** Deserializes a MiniMessage string into a Component. */
    fun String.mm(): Component = MM.deserialize(this)

    /** Deserializes a list of MiniMessage strings into a list of Components. */
    fun List<String>.mm(): List<Component> = this.map { it.mm() }

    /** Creates an ItemLore object from a single MiniMessage string. */
    fun String.il(): ItemLore.Builder = IL.addLine(this.mm())

    /** Creates an ItemLore object from a list of MiniMessage strings. */
    fun List<String>.il(): ItemLore.Builder = IL.addLines(this.mm())

    /** Creates a CustomModelData object from a single string. */
    fun String.cmd(): CustomModelData.Builder = CMD.addString(this)

    /** Creates a CustomModelData object from a list of strings. */
    fun List<String>.cmd(): CustomModelData.Builder = CMD.addStrings(this)
}