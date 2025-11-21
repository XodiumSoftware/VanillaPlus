package org.xodium.vanillaplus.enchantments

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.EnchantmentInterface

/** Represents an object handling feather falling enchantment implementation within the system. */
internal object FeatherFallingEnchantment : EnchantmentInterface {
    /**
     * Handles the PlayerInteractEvent to prevent farmland trampling when wearing boots with Feather Falling enchantment.
     * @param event The PlayerInteractEvent to handle.
     */
    fun featherFalling(event: PlayerInteractEvent) {
        if (event.action != Action.PHYSICAL) return
        if (event.clickedBlock?.type != Material.FARMLAND) return
        if (!isValidTool(event.player.inventory.boots)) return

        event.isCancelled = true
    }

    /**
     * Checks if the item is a pickaxe with Silk Touch.
     * @param item The item to check.
     * @return `true` if the item is a pickaxe with Silk Touch, otherwise `false`.
     */
    private fun isValidTool(item: ItemStack?): Boolean =
        item?.let { Tag.ITEMS_FOOT_ARMOR.isTagged(it.type) && it.containsEnchantment(Enchantment.FEATHER_FALLING) } == true
}
