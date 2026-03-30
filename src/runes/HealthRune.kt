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
import org.xodium.vanillaplus.runes.HealthRune.Companion.GEM
import org.xodium.vanillaplus.runes.HealthRune.Companion.MAX_TIERS
import org.xodium.vanillaplus.utils.Utils.MM

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

        /** Raw gem dropped by bosses. Combine with a container at a crafting table to forge a Health Rune. */
        val GEM: ItemStack =
            ItemStack.of(Material.ECHO_SHARD).apply {
                setData(
                    DataComponentTypes.ITEM_NAME,
                    MM.deserialize("<!italic><gradient:#CB2D3E:#EF473A><b>Health Gem</b></gradient>"),
                )
                setData(DataComponentTypes.MAX_STACK_SIZE, 16)
                setData(DataComponentTypes.ITEM_MODEL, NamespacedKey(instance, "rune/health_gem"))
            }

        /** Copper container — crafted from copper ingots; yields [tiers][0] when combined with [GEM]. */
        val CONTAINER_COPPER: ItemStack = containerItem("copper", Material.COPPER_INGOT, "#B87333", "#DA8A67")

        /** Iron container — crafted from iron ingots + Health Rune V; yields [tiers][5] when combined with [GEM]. */
        val CONTAINER_IRON: ItemStack = containerItem("iron", Material.IRON_INGOT, "#B0B0B0", "#E0E0E0")

        /** Gold container — crafted from gold ingots + Health Rune X; yields [tiers][10] when combined with [GEM]. */
        val CONTAINER_GOLD: ItemStack = containerItem("gold", Material.GOLD_INGOT, "#FFD700", "#FFA500")

        /** Diamond container — crafted from diamonds + Health Rune XV; yields [tiers][15] when combined with [GEM]. */
        val CONTAINER_DIAMOND: ItemStack = containerItem("diamond", Material.DIAMOND, "#7DF9FF", "#00BFFF")

        private fun containerItem(
            group: String,
            material: Material,
            colorFrom: String,
            colorTo: String,
        ): ItemStack =
            ItemStack.of(material).apply {
                setData(
                    DataComponentTypes.ITEM_NAME,
                    MM.deserialize(
                        "<!italic><gradient:$colorFrom:$colorTo><b>${group.replaceFirstChar { it.titlecase() }}" +
                            " Rune Container</b></gradient>",
                    ),
                )
                setData(DataComponentTypes.MAX_STACK_SIZE, 1)
                setData(DataComponentTypes.ITEM_MODEL, NamespacedKey(instance, "rune/container_$group"))
            }

        /** Returns the texture group name for [tier]: copper (I–V), iron (VI–X), gold (XI–XV), diamond (XVI–XX). */
        private fun tierGroup(tier: Int): String =
            when {
                tier <= 5 -> "copper"
                tier <= 10 -> "iron"
                tier <= 15 -> "gold"
                else -> "diamond"
            }

        private fun toRoman(n: Int): String {
            val values = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
            val symbols = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
            var num = n
            val sb = StringBuilder()
            for (i in values.indices) {
                while (num >= values[i]) {
                    sb.append(symbols[i])
                    num -= values[i]
                }
            }
            return sb.toString()
        }
    }

    override val id: String = "${FAMILY}_$tier"
    override val family: String = FAMILY
    override val droppable: Boolean = false

    override fun nextTier(): RuneInterface? = if (tier < MAX_TIERS) tiers[tier] else null

    override val item: ItemStack =
        ItemStack.of(Material.AMETHYST_SHARD).apply {
            setData(
                DataComponentTypes.ITEM_NAME,
                MM.deserialize("<!italic><gradient:#CB2D3E:#EF473A><b>Health Rune ${toRoman(tier)}</b></gradient>"),
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
