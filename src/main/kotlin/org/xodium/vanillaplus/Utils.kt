/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.block.Block
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import java.util.*


/**
 * Provides utility functions for directory creation and file copying within the plugin.
 */
object Utils {
    private val logger = instance.logger
    val MM: MiniMessage = MiniMessage.miniMessage()
    fun String.mm() = MM.deserialize(this)
    fun List<String>.mm() = map { it.mm() }

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
                    ?.let { NamespacedKey.minecraft(it) }
                    ?.let(Registry.SOUNDS::get)
                    ?: fallbackSound,
                volume.toFloat(),
                pitch.toFloat())
        } catch (ex: Exception) {
            logger.severe("Failed to play sound '${sound ?: fallbackSound}' at block '${block.location}': ${ex.message}")
        }
    }
}
