package org.xodium.vanillaplus.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.attribute.Attribute

/** Serializer for the [Attribute] class used in Bukkit. */
internal object AttributeSerializer : KSerializer<Attribute> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Attribute", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Attribute,
    ) {
        encoder.encodeString(value.key.toString())
    }

    override fun deserialize(decoder: Decoder): Attribute {
        val key =
            NamespacedKey.fromString(decoder.decodeString())
                ?: throw IllegalArgumentException("Invalid NamespacedKey")
        return Registry.ATTRIBUTE.get(key)
            ?: throw IllegalArgumentException("Unknown attribute: $key")
    }
}
