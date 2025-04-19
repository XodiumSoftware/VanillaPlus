/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Database
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mm
import java.net.URI


/**
 * Represents a module handling waystone mechanics within the system.
 * A waystone allows players to teleport between locations using specific in-game constructs.
 * The module listens for relevant player and block events and manages waystone functionality accordingly.
 *
 * Features:
 * - Automatically enables if the module is configured as enabled.
 * - Initializes the required database table and crafting recipe on enabling.
 * - Provides event handling for player interactions, block placements, and block breaking.
 * - Manages the GUI for interacting with waystones.
 */
class WaystoneModule : ModuleInterface {
    override fun enabled(): Boolean = Config.WaystoneModule.ENABLED

    private val packInfo = ResourcePackInfo.resourcePackInfo()
        .uri(URI.create("https://example.com/resourcepack.zip"))
        .hash("2849ace6aa689a8c610907a41c03537310949294")
        .build()
    private val pack = ResourcePackRequest.resourcePackRequest()
        .packs(packInfo)
        .required(true)
        .build()
    private val recipeKey = NamespacedKey(instance, "waystone")
    private var waystoneLocations: MutableList<Location> = mutableListOf()

    init {
        if (enabled()) {
            Database.createTable(this::class)
            instance.server.addRecipe(recipe(recipeKey, waystoneItem()))
            val allData = Database.getData(this::class)
            val allKeys = if (allData is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                allData as Map<String, String?>
            } else emptyMap()
            val keys: List<String> = allKeys.keys.toList()
            val locations: List<Location> = keys.mapNotNull { keyToLocation(it) }
            waystoneLocations = locations.toMutableList()
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        event.player.sendResourcePacks(pack)
        //TODO: make it load a resourcepack, for the waystones.
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockPlaceEvent) {
        val itemMeta = event.itemInHand.itemMeta
        if (itemMeta != null && itemMeta.hasCustomModelData() && itemMeta.customModelData == 1) {
            waystoneLocations.add(event.blockPlaced.location)
            Database.setData(this::class, locationToKey(event.blockPlaced.location))
            event.player.sendActionBar("Waypoint has been created".fireFmt().mm())
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockBreakEvent) {
        val block = event.block
        if (block.type == Material.STONE_BRICKS && waystoneLocations.any { it == block.location }) {
            waystoneLocations.removeIf { it == block.location }
            Database.deleteData(this::class, locationToKey(event.block.location))
            event.player.sendActionBar("Waypoint has been deleted".fireFmt().mm())
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
        ItemStack(Material.STONE_BRICKS).apply {
            itemMeta = itemMeta.apply {
                customName("Waystone".mm())
                setCustomModelData(1)
            }
        }

    private fun pageNavItem(label: String): ItemStack =
        ItemStack(Material.ARROW).apply {
            itemMeta = itemMeta.apply { displayName(label.mm()) }
        }

    private fun locationToKey(location: Location) =
        "waystone:${location.world.name}:${location.blockX}:${location.blockY}:${location.blockZ}"

    private fun keyToLocation(key: String): Location? {
        val parts = key.split(":")
        if (parts.size != 5) return null
        val world = Bukkit.getWorld(parts[1]) ?: return null
        val x = parts[2].toIntOrNull() ?: return null
        val y = parts[3].toIntOrNull() ?: return null
        val z = parts[4].toIntOrNull() ?: return null
        return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
    }

    override fun recipe(key: NamespacedKey, item: ItemStack): Recipe =
        ShapedRecipe(key, item).apply {
            shape(" A ", "ABA", "AAA")
            setIngredient('A', Material.STONE_BRICKS)
            setIngredient('B', Material.ENDER_PEARL)
        }

    //TODO: create also a back button.
    //TODO: make sure that where the nav items are that whole row doesnt display waystones.
    //TODO: make waystones clickable, which will teleport you to their position.
    //TODO: add teleportation effects.
    //TODO: add teleportation xp cost.
    //TODO: Optional, do we add that you have to discover waypoints manually first before being able to use them?
    override fun gui(): Inventory {
        val total = waystoneLocations.size
        val size = ((total + 8) / 9).coerceIn(1, 6) * 9
        val inv = Bukkit.createInventory(null, size, "Ancient Teleportation Network".fireFmt().mm())
        waystoneLocations.take(size).forEachIndexed { i, _ -> inv.setItem(i, waystoneItem()) }
        if (total > size) inv.setItem(size - 1, pageNavItem("Next Page"))
        return inv
    }
}