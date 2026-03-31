package org.xodium.vanillaplus.runes

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.RuneInterface
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.buildItem
import org.xodium.vanillaplus.runes.CrimsoniteRune.Companion.MAX_TIERS
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.toRoman

/** Represents a tiered rune that increases the player's maximum health. Each tier grants +8 max health. */
internal class CrimsoniteRune private constructor(
    override val tier: Int,
) : RuneInterface {
    companion object {
        private const val HEALTH_PER_TIER = 8.0
        const val MAX_TIERS = 5

        /** All tiers of [CrimsoniteRune], from I to [MAX_TIERS]. */
        val tiers: List<CrimsoniteRune> = (1..MAX_TIERS).map { CrimsoniteRune(it) }
    }

    override val id: String = "${javaClass.simpleName}_$tier"

    override fun nextTier(): RuneInterface? = tiers.getOrNull(tier)

    override val item: ItemStack =
        buildItem(
            id = id,
            tier = tier,
            material = Material.AMETHYST_SHARD,
            name = MM.deserialize("<!italic><gradient:#CB2D3E:#EF473A><b>Crimsonite ${tier.toRoman()}</b></gradient>"),
            modifierLine = MM.deserialize("<!italic><blue>+${(tier * HEALTH_PER_TIER).toInt()} Max Health"),
        )

    override fun modifiers(
        player: Player,
        equipped: Boolean,
    ) {
        val attr = player.getAttribute(Attribute.MAX_HEALTH) ?: return

        attr.modifiers.filter { it.key == modifierKey }.forEach { attr.removeModifier(it) }

        if (equipped) {
            attr.addModifier(
                AttributeModifier(
                    modifierKey,
                    tier * HEALTH_PER_TIER,
                    AttributeModifier.Operation.ADD_NUMBER,
                ),
            )
        }
    }
}
