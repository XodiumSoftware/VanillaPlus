package org.xodium.vanillaplus.data

import org.bukkit.entity.EntityType
import org.xodium.vanillaplus.interfaces.RuneInterface

/** Represents a single entry in the rune drop table. */
internal data class RuneDropTableData(
    /** The entity type this entry applies to. */
    val entityType: EntityType,
    /** The probability (0.0–1.0) that a rune drops when this entity is killed. */
    val chance: Double,
    /** The specific runes that can drop from this entity. If empty, nothing drops. */
    val runes: List<RuneInterface>,
)
