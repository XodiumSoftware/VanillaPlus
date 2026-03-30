@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.interfaces

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import io.papermc.paper.datacomponent.item.ItemLore
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents a contract for runes within the system. */
internal interface RuneInterface {
    companion object {
        /** The [NamespacedKey] used to tag rune items with their type identifier. */
        val RUNE_TYPE_KEY = NamespacedKey(instance, "rune_type")

        /**
         * Builds the [ItemStack] for a rune tier from its varying properties.
         * Shared structure (lore header, stack size, CMD, PDC) is applied automatically.
         */
        fun buildItem(
            id: String,
            tier: Int,
            material: Material,
            name: Component,
            modifierLine: Component,
        ): ItemStack =
            ItemStack.of(material).apply {
                setData(DataComponentTypes.ITEM_NAME, name)
                setData(
                    DataComponentTypes.LORE,
                    ItemLore
                        .lore()
                        .addLines(listOf(Component.empty(), MM.deserialize("<!italic><gray>Modifiers:"), modifierLine)),
                )
                setData(DataComponentTypes.MAX_STACK_SIZE, 1)
                setData(
                    DataComponentTypes.ITEM_MODEL,
                    NamespacedKey(instance, "rune/${id.substringBefore("_").lowercase()}s"),
                )
                setData(
                    DataComponentTypes.CUSTOM_MODEL_DATA,
                    CustomModelData.customModelData().addFloat(tier.toFloat()).build(),
                )
                editPersistentDataContainer { it.set(RUNE_TYPE_KEY, PersistentDataType.STRING, id) }
            }
    }

    /**
     * A stable identifier for this rune, used as the PDC tag value and for slot persistence.
     * Defaults to the implementing class simple name.
     */
    val id: String get() = javaClass.simpleName

    /**
     * A unique [NamespacedKey] for this rune's attribute modifier, derived from [id].
     * Automatically unique per rune type — no override needed.
     */
    val modifierKey: NamespacedKey get() = NamespacedKey(instance, "rune_${id.lowercase()}_modifier")

    /** The gem [ItemStack] that represents this rune in the world and in the rune menu. */
    val item: ItemStack

    /**
     * Returns the next upgrade tier of this rune, or `null` if this is the maximum tier.
     * Used by the anvil combining mechanic.
     */
    fun nextTier(): RuneInterface? = null

    /**
     * Applies or removes this rune's modifier on the given [player].
     * @param player The player to modify.
     * @param equipped Whether this rune is currently equipped.
     */
    fun modifiers(
        player: Player,
        equipped: Boolean,
    )
}
