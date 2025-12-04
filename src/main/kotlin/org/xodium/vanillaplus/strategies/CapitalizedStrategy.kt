package org.xodium.vanillaplus.strategies

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.JsonNamingStrategy

/** A naming strategy that capitalizes the first letter of JSON property names. */
@ExperimentalSerializationApi
internal object CapitalizedStrategy : JsonNamingStrategy {
    override fun serialNameForJson(
        descriptor: SerialDescriptor,
        elementIndex: Int,
        serialName: String,
    ): String = serialName.replaceFirstChar { it.uppercase() }
}
