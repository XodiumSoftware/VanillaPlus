package org.xodium.vanillaplus.data

/** Defines the layout and spacing configuration for a horde formation. */
internal data class FormationData(
    /**
     * 2D character matrix defining the formation layout (front → rear along +Z).
     * Each string is a row; each character is a column (centered on X=0).
     * Supported characters: G=Goblin, O=Orc, T=Troll, D=DarkKnight, W=Warlord, else=empty.
     */
    val layout: List<String>,
    /** The distance between entities within a row (X axis). */
    val formationSpacing: Double = 4.0,
    /** The distance between rows (Z axis). */
    val rowSpacing: Double = 8.0,
)
