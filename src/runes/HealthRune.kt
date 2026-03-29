package org.xodium.vanillaplus.runes

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.interfaces.RuneInterface
import org.xodium.vanillaplus.modules.RuneModule
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents a rune that increases the player's maximum health. */
internal object HealthRune : RuneInterface {
    private const val HEALTH_PER_RUNE = 2.0

    @Suppress("UnstableApiUsage")
    override val item: ItemStack =
        ItemStack.of(Material.AMETHYST_SHARD).apply {
            setData(
                DataComponentTypes.ITEM_NAME,
                MM.deserialize("<!italic><gradient:#CB2D3E:#EF473A>Health Rune</gradient>"),
            )
            setData(
                DataComponentTypes.LORE,
                ItemLore.lore().addLines(
                    listOf(
                        MM.deserialize(
                            "<!italic><gray>Grants +${HEALTH_PER_RUNE} max health when slotted</gray>",
                        ),
                    ),
                ),
            )
            editPersistentDataContainer { it.set(RuneModule.RUNE_TYPE_KEY, PersistentDataType.STRING, id) }
        }

    override fun modifiers(
        player: Player,
        count: Int,
    ) {
        val attr = player.getAttribute(Attribute.MAX_HEALTH) ?: return

        attr.modifiers.filter { it.key == modifierKey }.forEach { attr.removeModifier(it) }

        if (count > 0) {
            attr.addModifier(
                AttributeModifier(
                    modifierKey,
                    count * HEALTH_PER_RUNE,
                    AttributeModifier.Operation.ADD_NUMBER,
                ),
            )
        }
    }
}
