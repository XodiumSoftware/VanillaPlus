package org.xodium.vanillaplus.interfaces

/** Interface for kingdom government types. */
internal interface KingdomTypeInterface {
    val displayName: String
    val description: String

    /** Tribal government type - clan-based leadership with chieftains. */
    object Tribal : KingdomTypeInterface {
        override val displayName: String = "Tribal"
        override val description: String = "Clan-based leadership with chieftains"
    }

    /** Feudalism government type - hierarchical system with lords and vassals. */
    object Feudalism : KingdomTypeInterface {
        override val displayName: String = "Feudalism"
        override val description: String = "Hierarchical system with lords and vassals"
    }

    /** Autocracy government type - single ruler with absolute power. */
    object Autocracy : KingdomTypeInterface {
        override val displayName: String = "Autocracy"
        override val description: String = "Single ruler with absolute power"
    }

    /** Republic government type - elected representatives govern. */
    object Republic : KingdomTypeInterface {
        override val displayName: String = "Republic"
        override val description: String = "Elected representatives govern"
    }

    /** Theocracy government type - religious leadership governs. */
    object Theocracy : KingdomTypeInterface {
        override val displayName: String = "Theocracy"
        override val description: String = "Religious leadership governs"
    }
}
