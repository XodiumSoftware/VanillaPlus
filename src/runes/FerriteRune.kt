package org.xodium.vanillaplus.runes

import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.xodium.vanillaplus.interfaces.RuneInterface
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.buildTiers
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.toRoman

/** Represents a tiered rune that increases the player's attack damage. Each tier grants +1 attack damage. */
internal class FerriteRune private constructor(
    override val tier: Int,
) : RuneInterface {
    companion object {
        /** All tiers of [FerriteRune], from I to V. */
        val tiers: List<FerriteRune> = buildTiers(::FerriteRune)
    }

    override val tiers: List<RuneInterface> get() = Companion.tiers
    override val name: Component =
        MM.deserialize("<!italic><gradient:#C0C0C0:#707070><b>Ferrite ${tier.toRoman()}</b></gradient>")
    override val attribute: Attribute = Attribute.ATTACK_DAMAGE
    override val modifier: Double = 1.0
    override val modifierLore: Component = MM.deserialize("<!italic><blue>+${(tier * modifier).toInt()} Attack Damage")
}
