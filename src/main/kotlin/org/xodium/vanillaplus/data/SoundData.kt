/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import net.kyori.adventure.sound.Sound
import org.bukkit.Sound as BukkitSound

/**
 * Represents sound data with its properties such as name, source, volume, and pitch.
 * @property name The name of the sound.
 * @property source The source of the sound.
 * @property volume The volume of the sound.
 * @property pitch The pitch of the sound.
 */
data class SoundData(
    val name: BukkitSound,
    val source: Sound.Source,
    val volume: Float,
    val pitch: Float
) {
    /**
     * Converts this SoundData instance to a Sound instance.
     * @return A Sound instance with the properties of this SoundData.
     */
    fun toSound(): Sound = Sound.sound(name, source, volume, pitch)
}