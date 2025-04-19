/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.FmtUtils.mm


class WaystoneModule : ModuleInterface {
    override fun enabled(): Boolean = Config.WaystoneModule.ENABLED

    private val recipeKey = NamespacedKey(instance, "waystone")
    private val waystoneLocations = mutableListOf<Location>()

    init {
        if (enabled()) {
            instance.server.addRecipe(recipe(recipeKey, waystoneItem()))
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockPlaceEvent) {
        val itemMeta = event.itemInHand.itemMeta
        if (itemMeta != null && itemMeta.hasCustomModelData() && itemMeta.customModelData == 1) {
            waystoneLocations.add(event.blockPlaced.location)
            //TODO: send player message?
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        if (block.type == Material.STONE_BRICKS && waystoneLocations.any { it == block.location }) {
            event.isCancelled = true
            event.player.openInventory(gui())
        }
    }

    private fun waystoneItem(): ItemStack =
        ItemStack.of(Material.STONE_BRICKS).apply {
            itemMeta = itemMeta.apply {
                customName("Waystone".mm())
                setCustomModelData(1)
            }
        }

    override fun recipe(key: NamespacedKey, item: ItemStack): Recipe =
        ShapedRecipe(key, item).apply {
            shape(" A ", "ABA", "AAA")
            setIngredient('A', Material.STONE_BRICKS)
            setIngredient('B', Material.ENDER_PEARL)
        }

    override fun gui(): Inventory =
        Bukkit.createInventory(null, 9, "Waystone".mm()).apply {
            setItem(4, ItemStack(Material.ENDER_PEARL).apply {
                itemMeta = itemMeta?.apply { displayName("Teleport".mm()) }
            })
        }
}