package org.xodium.vanillaplus.runes

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.RuneInterface
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.buildItem
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

    override val attribute: Attribute = Attribute.MAX_HEALTH
    override val valuePerTier: Double = 8.0
    override val item: ItemStack =
        buildItem(
            id = id,
            tier = tier,
            material = Material.AMETHYST_SHARD,
            name = MM.deserialize("<!italic><gradient:#CB2D3E:#EF473A><b>Crimsonite ${tier.toRoman()}</b></gradient>"),
            modifierLine = MM.deserialize("<!italic><blue>+${(tier * valuePerTier).toInt()} Max Health"),
        )

    override fun nextTier(): RuneInterface? = tiers.getOrNull(tier)
}
