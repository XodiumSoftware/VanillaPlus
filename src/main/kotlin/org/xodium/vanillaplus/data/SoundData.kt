package org.xodium.vanillaplus.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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
    @Serializable(with = SoundTypeSerializer::class)
    val name: Sound.Type,
    private val source: Sound.Source = Sound.Source.MASTER,
    private val volume: Float = 1.0f,
    private val pitch: Float = 1.0f,
) {
    object SoundTypeSerializer : KSerializer<Sound.Type> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Sound.Type", PrimitiveKind.STRING)

        override fun serialize(
            encoder: Encoder,
            value: Sound.Type,
        ) {
            encoder.encodeString(value.key().asString())
        }

        override fun deserialize(decoder: Decoder): Sound.Type {
            val keyString = decoder.decodeString()
            return Sound.Type { Key.key(keyString) }
        }
    }

    /**
     * Converts this [SoundData] instance to a [Sound] instance.
     * @return A [Sound] instance with the properties of this [SoundData].
     */
    fun toSound(): Sound = Sound.sound(name, source, volume, pitch)
}
