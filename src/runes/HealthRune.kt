@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.runes

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.RuneInterface
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.RUNE_FAMILY_KEY
import org.xodium.vanillaplus.interfaces.RuneInterface.Companion.RUNE_TYPE_KEY
import org.xodium.vanillaplus.runes.HealthRune.Companion.MAX_TIERS
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.toRoman

/** Represents a tiered rune that increases the player's maximum health. Each tier grants +2 max health. */
internal class HealthRune private constructor(
    val tier: Int,
) : RuneInterface {
    companion object {
        const val FAMILY = "HealthRune"
        private const val HEALTH_PER_TIER = 2.0
        const val MAX_TIERS = 20

        /** All tiers of [HealthRune], from I to [MAX_TIERS]. */
        val tiers: List<HealthRune> = (1..MAX_TIERS).map { HealthRune(it) }

        /** Returns the texture group name for [tier]: copper (I–V), iron (VI–X), gold (XI–XV), diamond (XVI–XX). */
        private fun tierGroup(tier: Int): String =
            when {
                tier <= 5 -> "copper"
                tier <= 10 -> "iron"
                tier <= 15 -> "gold"
                else -> "diamond"
            }

    }

    override val id: String = "${FAMILY}_$tier"
    override val family: String = FAMILY
    override val droppable: Boolean = tier == 1

    override fun nextTier(): RuneInterface? = if (tier < MAX_TIERS) tiers[tier] else null

    override val item: ItemStack =
        ItemStack.of(Material.AMETHYST_SHARD).apply {
            setData(
                DataComponentTypes.ITEM_NAME,
                MM.deserialize("<!italic><gradient:#CB2D3E:#EF473A><b>Health Rune ${tier.toRoman()}</b></gradient>"),
            )
            setData(
                DataComponentTypes.LORE,
                ItemLore.lore().addLines(
                    listOf(
                        MM.deserialize("<!italic><gray>Modifiers:"),
                        MM.deserialize("<!italic><blue>+${(tier * HEALTH_PER_TIER).toInt()} Max Health <red>\u2665"),
                    ),
                ),
            )
            setData(DataComponentTypes.MAX_STACK_SIZE, 1)
            setData(DataComponentTypes.ITEM_MODEL, NamespacedKey(instance, "rune/health_${tierGroup(tier)}"))
            editPersistentDataContainer {
                it.set(RUNE_TYPE_KEY, PersistentDataType.STRING, id)
                it.set(RUNE_FAMILY_KEY, PersistentDataType.STRING, family)
            }
        }

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
