package org.xodium.illyriaplus.data

/**
 * Represents a pair of linked rituals (source and destination).
 *
 * @property source The source ritual location.
 * @property destination The destination ritual location.
 */
internal data class RitualPairData(
    val source: RitualLocationData,
    val destination: RitualLocationData,
)
