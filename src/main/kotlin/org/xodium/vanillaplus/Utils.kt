/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.util.*


/**
 * Provides utility functions for directory creation and file copying within the plugin.
 */
object Utils {
    val MM = MiniMessage.miniMessage()
    val fillerItem = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).name("".mm()).asGuiItem()
    val backItem = ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
        .name(firewatchFormat("Back").mm())
        .lore(listOf("<dark_gray>✖ <gray>Return to the previous menu").mm())
        .asGuiItem { player, _ -> Gui.faqGUI().open(player) }

    fun firewatchFormat(text: String) = "<b><gradient:#CB2D3E:#EF473A>$text</gradient></b>"
    fun mangoFormat(text: String) = "<b><gradient:#FFE259:#FFA751>$text</gradient></b>"
    fun worldSizeFormat(size: Int) = if (size >= 1000) "${size / 1000}k" else size.toString()

    fun String.mm() = MM.deserialize(this)
    fun List<String>.mm() = map { it.mm() }

    fun EntityType.format(locale: Locale = Locale.ENGLISH, delimiters: String = "_", separator: String = " ") =
        name.lowercase(locale).split(delimiters).joinToString(separator)
        { it.replaceFirstChar { char -> char.uppercaseChar() } }

    fun List<EntityType>.format(separator: String) = this.joinToString(separator) { it.format() }

    /**
     * A helper function to wrap command execution with standardized error handling.
     *
     * @param ctx The CommandContext used to obtain the CommandSourceStack.
     * @param action The action to execute, receiving a CommandSourceStack as a parameter.
     * @return Command.SINGLE_SUCCESS after execution.
     */
    fun tryCatch(ctx: CommandContext<CommandSourceStack>, action: (CommandSourceStack) -> Unit): Int {
        try {
            action(ctx.source)
        } catch (e: Exception) {
            instance.logger.severe("An Error has occured: ${e.message}")
            e.printStackTrace()
            (ctx.source.sender as Player).sendMessage("${VanillaPlus.PREFIX}<red>An Error has occured. Check server logs for details.".mm())
        }
        return Command.SINGLE_SUCCESS
    }
}