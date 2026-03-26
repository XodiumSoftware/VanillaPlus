package org.xodium.vanillaplus.data

import org.bukkit.entity.Mob
import org.bukkit.util.Vector

/** Represents a single member of a horde formation. */
internal data class FormationMemberData(
    /** The [Mob] entity belonging to this formation slot. */
    val mob: Mob,
    /** The [Vector] offset from the warlord used when the formation is actively marching. */
    val marchOffset: Vector,
    /** The angle (in radians) of this member's slot on the idle circle around the warlord. */
    val idleAngle: Double,
)
