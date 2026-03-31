package org.xodium.vanillaplus.runes

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.RuneInterface
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.buildItem
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.buildTiers
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.toRoman

/** Represents a tiered rune that increases the player's movement speed. Each tier grants +10% move speed. */
internal class ZephyriteRune private constructor(
    override val tier: Int,
) : RuneInterface {
    companion object {
        /** All tiers of [ZephyriteRune], from I to V. */
        val tiers: List<ZephyriteRune> = buildTiers(::ZephyriteRune)
    }

    override val attribute: Attribute = Attribute.MOVEMENT_SPEED
    override val valuePerTier: Double = 0.01
    override val item: ItemStack =
        buildItem(
            id = id,
            tier = tier,
            material = Material.FEATHER,
            name = MM.deserialize("<!italic><gradient:#56CCF2:#2F80ED><b>Zephyrite ${tier.toRoman()}</b></gradient>"),
            modifierLine = MM.deserialize("<!italic><blue>+${tier * 10}% Move Speed"),
        )

    override fun nextTier(): RuneInterface? = tiers.getOrNull(tier)
}
