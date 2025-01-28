/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.interfaces.ModuleInterface

class CommandPanelModule : ModuleInterface {

    companion object {
        private const val INVENTORY_SIZE = 27
        private const val FAQ_ITEM_SLOT = 13
        private val INVENTORY_TITLE = Component.text("FAQ Panel")
    }

    private lateinit var faqInventory: Inventory

    override fun init() {
        faqInventory = createFaqInventory()
    }

    private fun createFaqInventory(): Inventory =
        Bukkit.createInventory(null, INVENTORY_SIZE, INVENTORY_TITLE).apply {
            setItem(FAQ_ITEM_SLOT, createFaqItem())
        }

    private fun createFaqItem(): ItemStack =
        ItemStack(Material.PAPER).apply {
            itemMeta = itemMeta?.apply {
                displayName(INVENTORY_TITLE)
                lore(listOf(Component.text("This is the FAQ Panel")))
            }
        }

    fun openInventory(player: Player) {
        player.openInventory(faqInventory)
    }

    override fun enabled(): Boolean = Config.CommandPanelModule.ENABLE
}