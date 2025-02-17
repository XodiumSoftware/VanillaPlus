/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.util.*


/**
 * Provides utility functions for directory creation and file copying within the plugin.
 */
object Utils {
    val MM: MiniMessage = MiniMessage.miniMessage()
    val antiSpamDuration = Config.GUI_ANTI_SPAM_DURATION
    val fillerItem = ItemBuilder.from(Material.BLACK_STAINED_GLASS_PANE).name("".mm()).asGuiItem()
    val backItem = ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
        .name(birdflopFormat("Back"))
        .lore(listOf("<dark_gray>✖ <gray>Return to the previous menu").mm())
        .asGuiItem { player, _ -> Gui.faqGUI().open(player) }

    fun birdflopFormat(text: String): Component = "<b><gradient:#CB2D3E:#EF473A>$text</gradient></b>".mm()
    fun mangoFormat(text: String): Component = "<b><gradient:#FFE259:#FFA751>$text</gradient></b>".mm()
    fun worldSizeFormat(size: Int): String = if (size >= 1000) "${size / 1000}k" else size.toString()

    fun String.mm() = MM.deserialize(this)
    fun List<String>.mm() = map { it.mm() }
    fun EntityType.format(): String = name.lowercase(Locale.ENGLISH)
        .split("_")
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercaseChar() } }

    fun List<EntityType>.formatList(): String = this.joinToString(" <red>and the<dark_red> ") { it.format() }

    /**
     * Plays a sound at the location of the specified block.
     *
     * @param block The block at whose location the sound will be played.
     * @param sound The name of the sound to play. If null or the sound is not found, the fallback sound will be used.
     * @param fallbackSound The sound to play if the specified sound is not found or is null.
     * @param volume The volume at which to play the sound. This should be a positive integer.
     * @param pitch The pitch at which to play the sound. This should be a positive integer.
     */
    fun playSound(block: Block, sound: String?, fallbackSound: Sound, volume: Int, pitch: Int) {
        try {
            block.world.playSound(
                block.location,
                sound
                    ?.lowercase(Locale.getDefault())
                    ?.let(NamespacedKey::minecraft)
                    ?.let(Registry.SOUNDS::get)
                    ?: fallbackSound,
                volume.toFloat(),
                pitch.toFloat()
            )
        } catch (ex: Exception) {
            instance.logger.severe("Failed to play sound '${sound ?: fallbackSound}' at block '${block.location}': ${ex.message}")
            ex.printStackTrace()
        }
    }

    /**
     * A helper function to wrap command execution with standardized error handling.
     *
     * @param ctx The CommandContext used to obtain the CommandSourceStack.
     * @param action The action to execute, receiving a CommandSourceStack as a parameter.
     * @return Command.SINGLE_SUCCESS after execution.
     */
    @Suppress("UnstableApiUsage")
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