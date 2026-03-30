@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.runes

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
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
import org.xodium.vanillaplus.runes.SpeedRune.Companion.MAX_TIERS
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.toRoman

/** Represents a tiered rune that increases the player's movement speed. Each tier grants +10% move speed. */
internal class SpeedRune private constructor(
    val tier: Int,
) : RuneInterface {
    companion object {
        const val FAMILY = "SpeedRune"
        private const val SPEED_PER_TIER = 0.01
        const val MAX_TIERS = 5

        /** All tiers of [SpeedRune], from I to [MAX_TIERS]. */
        val tiers: List<SpeedRune> = (1..MAX_TIERS).map { SpeedRune(it) }
    }

    override val id: String = "${FAMILY}_$tier"
    override val family: String = FAMILY
    override val droppable: Boolean = tier == 1

    override fun nextTier(): RuneInterface? = if (tier < MAX_TIERS) tiers[tier] else null

    override val item: ItemStack =
        ItemStack.of(Material.FEATHER).apply {
            setData(
                DataComponentTypes.ITEM_NAME,
                MM.deserialize("<!italic><gradient:#56CCF2:#2F80ED><b>Speed Rune ${tier.toRoman()}</b></gradient>"),
            )
            setData(
                DataComponentTypes.LORE,
                ItemLore.lore().addLines(
                    listOf(
                        MM.deserialize("<!italic><gray>Modifiers:"),
                        MM.deserialize("<!italic><blue>+${tier * 10}% Move Speed <white>\u26A1"),
                    ),
                ),
            )
            setData(DataComponentTypes.MAX_STACK_SIZE, 1)
            setData(DataComponentTypes.ITEM_MODEL, NamespacedKey(instance, "rune/speed"))
            setData(
                DataComponentTypes.CUSTOM_MODEL_DATA,
                CustomModelData.customModelData().addFloat(tier.toFloat()).build(),
            )
            editPersistentDataContainer {
                it.set(RUNE_TYPE_KEY, PersistentDataType.STRING, id)
                it.set(RUNE_FAMILY_KEY, PersistentDataType.STRING, family)
            }
        }

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
