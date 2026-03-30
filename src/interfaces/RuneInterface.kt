package org.xodium.vanillaplus.interfaces

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance

/** Represents a contract for runes within the system. */
internal interface RuneInterface {
    companion object {
        /** The [NamespacedKey] used to tag rune items with their type identifier. */
        val RUNE_TYPE_KEY = NamespacedKey(instance, "rune_type")

        /** The [NamespacedKey] used to tag rune items with their family, for duplicate slot detection. */
        val RUNE_FAMILY_KEY = NamespacedKey(instance, "rune_family")
    }

    /**
     * A stable identifier for this rune, used as the PDC tag value and for slot persistence.
     * Defaults to the implementing class simple name.
     */
    val id: String get() = javaClass.simpleName

    /**
     * The family identifier shared across all tiers of this rune type.
     * Used to prevent equipping multiple tiers of the same rune simultaneously. Defaults to [id].
     */
    val family: String get() = id

    /**
     * Whether this rune can drop from boss mobs. Higher tiers should override to `false`.
     */
    val droppable: Boolean get() = true

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
