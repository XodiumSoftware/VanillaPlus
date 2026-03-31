package org.xodium.vanillaplus.runes

import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.xodium.vanillaplus.interfaces.RuneInterface
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.buildTiers
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.toRoman

/** Represents a tiered rune that increases the player's luck. Each tier grants +1 luck. */
internal class AureliteRune private constructor(
    override val tier: Int,
) : RuneInterface {
    companion object {
        /** All tiers of [AureliteRune], from I to V. */
        val tiers: List<AureliteRune> = buildTiers(::AureliteRune)
    }

    override val tiers: List<RuneInterface> get() = Companion.tiers
    override val name: Component =
        MM.deserialize("<!italic><gradient:#FFD700:#FFA500><b>Aurelite ${tier.toRoman()}</b></gradient>")
    override val attribute: Attribute = Attribute.LUCK
    override val modifier: Double = 1.0
    override val modifierLore: Component = MM.deserialize("<!italic><blue>+${(tier * modifier).toInt()} Luck")
}
