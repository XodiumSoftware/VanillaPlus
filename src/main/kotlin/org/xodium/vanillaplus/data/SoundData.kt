/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.data

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.node.ObjectNode
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

object SoundTypeDeserializer : JsonDeserializer<Sound.Type>() {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): Sound.Type {
        return when (parser.currentToken) {
            JsonToken.VALUE_STRING -> Sound.Type { Key.key(parser.text) }
            JsonToken.START_OBJECT -> {
                val node = parser.codec.readTree<ObjectNode>(parser)
                node.get("key")?.let { keyNode ->
                    val namespace = keyNode.get("namespace").asText()
                    val key = keyNode.get("key").asText()
                    return Sound.Type { Key.key("$namespace:$key") }
                }
                node.path("holder").get("registeredName")?.asText(null)
                    ?.let { Sound.Type { Key.key(it) } }
                    ?: throw JsonMappingException.from(parser, "Cannot parse Sound.Type from $node")
            }

            else -> throw JsonMappingException.from(parser, "Unexpected token for Sound.Type: ${parser.currentToken}")
        }
    }
}

/**
 * Represents sound data with its properties such as name, source, volume, and pitch.
 * @property name The name of the sound.
 * @property source The source of the sound. Defaults to [Sound.Source.MASTER].
 * @property volume The volume of the sound. Defaults to 1.0f.
 * @property pitch The pitch of the sound. Defaults to 1.0f.
 */
data class SoundData(
    @JsonDeserialize(using = SoundTypeDeserializer::class)
    val name: Sound.Type,
    val source: Sound.Source = Sound.Source.MASTER,
    val volume: Float = 1.0f,
    val pitch: Float = 1.0f
) {
    /**
     * Converts this SoundData instance to a Sound instance.
     * @return A Sound instance with the properties of this SoundData.
     */
    fun toSound(): Sound = Sound.sound(name, source, volume, pitch)
}