package org.xodium.vanillaplus.enchantments

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.enchantments.FeatherFallingEnchantment.isValidTool
import org.xodium.vanillaplus.interfaces.EnchantmentInterface

/** Represents an object handling feather falling enchantment implementation within the system. */
internal object FeatherFallingEnchantment : EnchantmentInterface {
    /**
     * Prevents farmland from being trampled when the player wears Feather Falling boots.
     * Cancels [Action.PHYSICAL] interactions with [Material.FARMLAND] if [isValidTool] passes.
     * @param event The [PlayerInteractEvent] to handle.
     */
    fun onPlayerInteract(event: PlayerInteractEvent) {
        when {
            event.action != Action.PHYSICAL -> return
            event.clickedBlock?.type != Material.FARMLAND -> return
            !isValidTool(event.player.inventory.boots) -> return
            else -> event.isCancelled = true
        }
    }

    /**
     * Checks if the item is foot armor with the Feather Falling enchantment.
     * @param item The item to check.
     * @return `true` if the item is foot armor with Feather Falling, otherwise `false`.
     */
    private fun isValidTool(item: ItemStack?): Boolean =
        item?.let {
            Tag.ITEMS_FOOT_ARMOR.isTagged(it.type) && it.containsEnchantment(Enchantment.FEATHER_FALLING)
        } ?: false
}
