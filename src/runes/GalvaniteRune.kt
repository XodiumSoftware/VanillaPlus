package org.xodium.vanillaplus.runes

import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.xodium.vanillaplus.interfaces.RuneInterface
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.buildTiers
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.toRoman

/** Represents a tiered rune that increases the player's attack speed. Each tier grants +0.2 attack speed. */
internal class GalvaniteRune private constructor(
    override val tier: Int,
) : RuneInterface {
    companion object {
        /** All tiers of [GalvaniteRune], from I to V. */
        val tiers: List<GalvaniteRune> = buildTiers(::GalvaniteRune)
    }

    override val tiers: List<RuneInterface> get() = Companion.tiers
    override val name: Component =
        MM.deserialize("<!italic><gradient:#F7FF00:#1CD8D2><b>Galvanite ${tier.toRoman()}</b></gradient>")
    override val attribute: Attribute = Attribute.ATTACK_SPEED
    override val modifier: Double = 0.2
    override val modifierLore: Component =
        MM.deserialize("<!italic><blue>+${"%.1f".format(tier * modifier)} Attack Speed")
}
