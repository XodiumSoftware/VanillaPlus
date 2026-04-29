package org.xodium.illyriaplus.data

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

/**
 * Wrapper for serializing a collection of kingdoms.
 *
 * @property kingdoms List of all kingdoms.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
internal data class KingdomsData(
    val kingdoms: List<KingdomData> = emptyList(),
)
