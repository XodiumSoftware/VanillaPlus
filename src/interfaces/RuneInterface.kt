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
     * Applies or removes this rune's modifier on the given [player].
     * @param player The player to modify.
     * @param equipped Whether this rune is currently equipped.
     */
    fun modifiers(
        player: Player,
        equipped: Boolean,
    )
}
