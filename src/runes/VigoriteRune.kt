package org.xodium.vanillaplus.runes

import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.xodium.vanillaplus.interfaces.RuneInterface
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.buildTiers
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.toRoman

/** Represents a tiered rune that increases the player's max absorption. Each tier grants +4 absorption. */
internal class VigoriteRune private constructor(
    override val tier: Int,
) : RuneInterface {
    companion object {
        /** All tiers of [VigoriteRune], from I to V. */
        val tiers: List<VigoriteRune> = buildTiers(::VigoriteRune)
    }

    override val tiers: List<RuneInterface> get() = Companion.tiers
    override val name: Component =
        MM.deserialize("<!italic><gradient:#F7971E:#FFD200><b>Vigorite ${tier.toRoman()}</b></gradient>")
    override val attribute: Attribute = Attribute.MAX_ABSORPTION
    override val modifier: Double = 4.0
    override val modifierLore: Component = MM.deserialize("<!italic><blue>+${(tier * modifier).toInt()} Absorption")
}
