package org.xodium.vanillaplus.data

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.util.StdConverter
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

/**
 * Represents sound data with its properties such as [name], [source], [volume], and [pitch].
 * @property name The [name] of the sound.
 * @property source The [source] of the sound. Defaults to [Sound.Source.MASTER].
 * @property volume The [volume] of the sound. Defaults to 1.0f.
 * @property pitch The [pitch] of the sound. Defaults to 1.0f.
 */
internal data class SoundData(
    @get:JsonSerialize(converter = SoundTypeToString::class)
    @param:JsonDeserialize(converter = StringToSoundType::class)
    val name: Sound.Type,
    private val source: Sound.Source = Sound.Source.MASTER,
    private val volume: Float = 1.0f,
    private val pitch: Float = 1.0f,
) {
    companion object {
        /**
         * Converts a [Sound.Type] to its string representation for JSON serialization.
         * Converts a string back to a [Sound.Type] for JSON deserialization.
         */
        private object SoundTypeToString : StdConverter<Sound.Type, String>() {
            override fun convert(value: Sound.Type) = value.key().asString()
        }

        /**
         * Converts a string to a [Sound.Type] for JSON deserialization.
         * Converts a [Sound.Type] to its string representation for JSON serialization.
         */
        private object StringToSoundType : StdConverter<String, Sound.Type>() {
            override fun convert(value: String) = Sound.Type { Key.key(value) }
        }
    }

    /**
     * Converts this [SoundData] instance to a [Sound] instance.
     * @return A [Sound] instance with the properties of this [SoundData].
     */
    fun toSound(): Sound = Sound.sound(name, source, volume, pitch)
}
