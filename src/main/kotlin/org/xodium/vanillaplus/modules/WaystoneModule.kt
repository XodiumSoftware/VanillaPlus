/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.scheduler.BukkitRunnable
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Database
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.WaystoneData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.fromGson
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.FmtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.toGson
import org.xodium.vanillaplus.utils.TimeUtils.seconds
import java.net.URI
import java.util.*

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
    private var waystoneEntries: MutableList<WaystoneData> = mutableListOf()
    private val guiTitle = "<b>Waystone Network".fireFmt().mm()
    private val playerGuiOrigin = mutableMapOf<UUID, Location>()

    init {
        if (enabled()) {
            Database.createTable(this::class)
            instance.server.addRecipe(recipe(recipeKey, waystoneItem("Waystone".mm())))
            val allData = Database.getData(this::class)
            val allKeys = if (allData is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                allData as Map<String, String?>
            } else emptyMap()
            waystoneEntries = allKeys.mapNotNull { (key, value) ->
                val loc = keyToLocation(key) ?: return@mapNotNull null
                val displayNameComponent = value?.let { jsonString ->
                    try {
                        jsonString.fromGson()
                    } catch (e: Exception) {
                        instance.logger.warning("Failed to parse Waystone display name JSON for key $key: ${e.message}. Using default.")
                        "Waystone".mm()
                    }
                } ?: "Waystone".mm()
                WaystoneData(loc, displayNameComponent)
            }.toMutableList()
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
            val displayName = itemMeta.displayName() ?: "Waystone".mm()
            waystoneEntries.add(WaystoneData(event.blockPlaced.location, displayName))
            Database.setData(this::class, locationToKey(event.blockPlaced.location), displayName.toGson())
            event.player.sendActionBar("Waypoint has been created".fireFmt().mm())
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockBreakEvent) {
        val block = event.block
        val idx = waystoneEntries.indexOfFirst { it.location == block.location }
        if (block.type == Material.STONE_BRICKS && idx != -1) {
            waystoneEntries.removeAt(idx)
            Database.deleteData(this::class, locationToKey(event.block.location))
            event.player.sendActionBar("Waypoint has been deleted".fireFmt().mm())
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        if (block.type == Material.STONE_BRICKS && waystoneEntries.any { it.location == block.location }) {
            event.isCancelled = true
            val player = event.player
            playerGuiOrigin[player.uniqueId] = block.location
            player.openInventory(gui())
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        if (event.view.title() != guiTitle) return
        event.isCancelled = true
        val slot = event.rawSlot
        if (slot !in waystoneEntries.indices) return
        val targetData = waystoneEntries[slot]
        val originLoc = playerGuiOrigin[player.uniqueId]
        if (originLoc != null && targetData.location == originLoc) { //TODO: instead hide the clicked waystone in the gui.
            return player.sendActionBar("You cannot teleport to the waystone you are at".fireFmt().mm())
        }
        player.closeInventory()
        player.sendActionBar("Teleporting...".mangoFmt().mm())

        val initialLocation = player.location.clone()
        val delayTicks = 3.seconds

        object : BukkitRunnable() {
            var ticks = 0
            override fun run() {
                if (!player.isOnline || player.isDead) {
                    cancel()
                    return
                }
                if (player.location.distanceSquared(initialLocation) > 0.5) {
                    player.sendActionBar("Teleport cancelled (you moved)!".fireFmt().mm())
                    cancel()
                    return
                }

                player.world.spawnParticle(
                    Particle.PORTAL,
                    player.location.add(0.0, 1.0, 0.0),
                    20,
                    0.5,
                    1.0,
                    0.5,
                    0.1
                )
                player.world.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_HAT, 0.2f, 2.0f)

                ticks++
                if (ticks >= delayTicks) {
                    teleportEffects(player.location)
                    player.teleport(targetData.location)
                    teleportEffects(targetData.location)
                    player.sendActionBar(
                        "Teleported to ".fireFmt().mm()
                            .append(targetData.displayName)
                            .append("!".fireFmt().mm())
                    )
                    playerGuiOrigin.remove(player.uniqueId)
                    cancel()
                }
            }
        }.runTaskTimer(instance, 0L, 1L)
    }

    private fun teleportEffects(location: Location) {
        location.world?.spawnParticle(Particle.PORTAL, location, 60, 0.5, 1.0, 0.5, 0.2)
        location.world?.spawnParticle(Particle.END_ROD, location, 20, 0.2, 0.8, 0.2, 0.05)
        location.world?.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f)
    }

    private fun waystoneItem(customName: Component): ItemStack =
        ItemStack(Material.STONE_BRICKS).apply {
            itemMeta = itemMeta.apply {
                customName(customName)
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
            shape("   ", "CBC", "AAA")
            setIngredient('A', Material.STONE_BRICKS)
            setIngredient('B', Material.ENDER_PEARL)
            setIngredient('C', Material.COMPASS)
        }

    //TODO: add teleportation xp cost. cost based on distance between waystones.
    //TODO: Optional, do we add that you have to discover waypoints manually first before being able to use them?
    override fun gui(): Inventory {
        val total = waystoneEntries.size
        val rows = ((total + 8) / 9).coerceIn(2, 6)
        val size = rows * 9
        val inv = Bukkit.createInventory(null, size, guiTitle)
        val availableSlots = size - 9

        waystoneEntries.take(availableSlots).forEachIndexed { i, entry ->
            inv.setItem(i, waystoneItem(entry.displayName))
        }

        val bottomRowStart = size - 9
        if (total > availableSlots) inv.setItem(size - 1, pageNavItem("Next Page"))

        inv.setItem(bottomRowStart, pageNavItem("Previous Page"))
        return inv
    }
}