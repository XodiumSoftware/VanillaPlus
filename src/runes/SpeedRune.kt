package org.xodium.vanillaplus.runes

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.RuneInterface
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.buildItem
import org.xodium.vanillaplus.runes.SpeedRune.Companion.MAX_TIERS
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.toRoman

/** Represents a tiered rune that increases the player's movement speed. Each tier grants +10% move speed. */
internal class SpeedRune private constructor(
    val tier: Int,
) : RuneInterface {
    companion object {
        private const val SPEED_PER_TIER = 0.01
        const val MAX_TIERS = 5

        /** All tiers of [SpeedRune], from I to [MAX_TIERS]. */
        val tiers: List<SpeedRune> = (1..MAX_TIERS).map { SpeedRune(it) }
    }

    override val id: String = "${javaClass.simpleName}_$tier"

    override fun nextTier(): RuneInterface? = if (tier < MAX_TIERS) tiers[tier] else null

    override val item: ItemStack =
        buildItem(
            id = id,
            tier = tier,
            material = Material.FEATHER,
            name = MM.deserialize("<!italic><gradient:#56CCF2:#2F80ED><b>Zephyrite ${tier.toRoman()}</b></gradient>"),
            modifierLine = MM.deserialize("<!italic><blue>+${tier * 10}% Move Speed"),
        )

    override fun modifiers(
        player: Player,
        equipped: Boolean,
    ) {
        val attr = player.getAttribute(Attribute.MOVEMENT_SPEED) ?: return

        attr.modifiers.filter { it.key == modifierKey }.forEach { attr.removeModifier(it) }

        if (equipped) {
            attr.addModifier(
                AttributeModifier(
                    modifierKey,
                    tier * SPEED_PER_TIER,
                    AttributeModifier.Operation.ADD_NUMBER,
                ),
            )
        }
    }
}
