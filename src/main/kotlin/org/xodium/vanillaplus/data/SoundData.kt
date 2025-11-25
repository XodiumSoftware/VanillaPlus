package org.xodium.vanillaplus.data

import kotlinx.serialization.Serializable
import net.kyori.adventure.sound.Sound
import org.xodium.vanillaplus.serializers.SoundTypeSerializer

/**
 * Represents sound data with its properties such as [name], [source], [volume], and [pitch].
 * @property name The [name] of the sound.
 * @property source The [source] of the sound. Defaults to [Sound.Source.MASTER].
 * @property volume The [volume] of the sound. Defaults to 1.0f.
 * @property pitch The [pitch] of the sound. Defaults to 1.0f.
 */
@Serializable
internal data class SoundData(
    @Serializable(with = SoundTypeSerializer::class)
    val name: Sound.Type,
    private val source: Sound.Source = Sound.Source.MASTER,
    private val volume: Float = 1.0f,
    private val pitch: Float = 1.0f,
) {
    /**
     * Converts this [SoundData] instance to a [Sound] instance.
     * @return A [Sound] instance with the properties of this [SoundData].
     */
    fun toSound(): Sound = Sound.sound(name, source, volume, pitch)
}
