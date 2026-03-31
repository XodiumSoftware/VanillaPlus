package org.xodium.vanillaplus.runes

import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.xodium.vanillaplus.interfaces.RuneInterface
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.buildTiers
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.toRoman

/** Represents a tiered rune that increases the player's armor. Each tier grants +2 armor. */
internal class ObsiditeRune private constructor(
    override val tier: Int,
) : RuneInterface {
    companion object {
        /** All tiers of [ObsiditeRune], from I to V. */
        val tiers: List<ObsiditeRune> = buildTiers(::ObsiditeRune)
    }

    override val tiers: List<RuneInterface> get() = Companion.tiers
    override val name: Component =
        MM.deserialize("<!italic><gradient:#2C3E50:#7F8C8D><b>Obsidite ${tier.toRoman()}</b></gradient>")
    override val attribute: Attribute = Attribute.ARMOR
    override val modifier: Double = 2.0
    override val modifierLore: Component = MM.deserialize("<!italic><blue>+${(tier * modifier).toInt()} Armor")
}
