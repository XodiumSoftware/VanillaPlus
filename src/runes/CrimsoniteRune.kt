package org.xodium.vanillaplus.runes

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.xodium.vanillaplus.interfaces.RuneInterface
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.buildTiers
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.toRoman

/** Represents a tiered rune that increases the player's maximum health. Each tier grants +8 max health. */
internal class CrimsoniteRune private constructor(
    override val tier: Int,
) : RuneInterface {
    companion object {
        /** All tiers of [CrimsoniteRune], from I to V. */
        val tiers: List<CrimsoniteRune> = buildTiers(::CrimsoniteRune)
    }

    override val tiers: List<RuneInterface> get() = Companion.tiers
    override val name: Component =
        MM.deserialize("<!italic><gradient:#CB2D3E:#EF473A><b>Crimsonite ${tier.toRoman()}</b></gradient>")
    override val material: Material = Material.AMETHYST_SHARD
    override val attribute: Attribute = Attribute.MAX_HEALTH
    override val modifier: Double = 8.0
    override val modifierLore: Component = MM.deserialize("<!italic><blue>+${(tier * modifier).toInt()} Max Health")
}
