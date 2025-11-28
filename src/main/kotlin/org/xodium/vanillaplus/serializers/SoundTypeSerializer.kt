package org.xodium.vanillaplus.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

/**
 * A serializer for [Sound.Type] that converts it to and from its string representation.
 * This serializer encodes the [Sound.Type] as a string using its [Key] representation.
 * It decodes a string back into a [Sound.Type] using the [Key.key] method.
 * This is useful for serializing and deserializing sound types in a format that can be easily stored or transmitted.
 */
internal object SoundTypeSerializer : KSerializer<Sound.Type> {
    override val descriptor = PrimitiveSerialDescriptor("Sound/Type", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Sound.Type,
    ) {
        encoder.encodeString(value.key().asString())
    }

    override fun deserialize(decoder: Decoder): Sound.Type = Sound.Type { Key.key(decoder.decodeString()) }
}
