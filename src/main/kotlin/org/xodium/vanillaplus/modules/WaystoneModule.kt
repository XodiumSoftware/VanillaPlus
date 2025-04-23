/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import net.kyori.adventure.resource.ResourcePackRequest
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
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Database
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.WaystoneData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.FmtUtils.mm
import org.xodium.vanillaplus.utils.TimeUtils.seconds
import org.xodium.vanillaplus.utils.TimeUtils.ticks
import org.xodium.vanillaplus.utils.Utils
import java.util.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

//TODO: Optional, do we add that you have to discover waypoints manually first before being able to use them?
//TODO: Add waystone custom texture.
//TODO: Use PDC for WaystoneData. (in progress)
//TODO: Use library for GUI.

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
@OptIn(ExperimentalUuidApi::class)
class WaystoneModule : ModuleInterface {
    override fun enabled(): Boolean = Config.WaystoneModule.ENABLED

    private val waystones = mutableMapOf<Uuid, WaystoneData>()

    init {
        if (enabled()) {
            Database.createTable(this::class)
            instance.server.addRecipe(WaystoneData.recipe(WaystoneData.item(""))))
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        event.player.sendResourcePacks(
            ResourcePackRequest.resourcePackRequest()
                .packs(Config.WaystoneModule.RESOURCE_PACK_INFO)
                .required(Config.WaystoneModule.RESOURCE_PACK_FORCE)
                .build()
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockPlaceEvent) {
        val itemMeta = event.itemInHand.itemMeta
        if (itemMeta != null && itemMeta.hasCustomModelData() && itemMeta.customModelData == waystoneCustomModelData) {
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockBreakEvent) {
        val block = event.block
        val idx = waystoneEntries.indexOfFirst { it.location == block.location }
        if (block.type == Material.STONE_BRICKS && idx != -1) {
            waystoneEntries.removeAt(idx)
            Database.deleteData(this::class, WaystoneData.serialize(event.block.location))
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

        val originLoc = playerGuiOrigin[player.uniqueId]
        val filteredWaystones = waystoneEntries.filter { it.location != originLoc }

        if (slot !in filteredWaystones.indices) return
        val targetData = filteredWaystones[slot]

        val xpCost = WaystoneData.calculateXpCost(originLoc ?: player.location, targetData.location)
        if (player.gameMode in listOf(GameMode.SURVIVAL, GameMode.ADVENTURE)) {
            val playerTotalXp = player.level * 7 + player.exp.toInt()
            if (playerTotalXp < xpCost) {
                player.closeInventory()
                return player.sendActionBar("You need $xpCost XP to teleport to this waystone".fireFmt().mm())
            }
        }

        player.closeInventory()
        player.sendActionBar("Teleporting...".mangoFmt().mm())

        val initialLocation = player.location.clone()
        val delayTicks = 3.seconds

        var ticks = 0

        instance.server.scheduler.runTaskTimer(
            instance,
            { task ->
                if (!player.isOnline || player.isDead) {
                    task.cancel()
                    return@runTaskTimer
                }
                if (player.location.distanceSquared(initialLocation) > 0.5) {
                    player.sendActionBar("Teleport cancelled (you moved)!".fireFmt().mm())
                    task.cancel()
                    return@runTaskTimer
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
                    if (player.gameMode in listOf(GameMode.SURVIVAL, GameMode.ADVENTURE)) {
                        Utils.chargePlayerXp(player, xpCost)
                    }
                    teleportEffects(player.location)
                    player.teleport(targetData.location)
                    teleportEffects(targetData.location)
                    player.sendActionBar(
                        "Teleported to ".fireFmt().mm()
                            .append(targetData.displayName.mm())
                            .append("!".fireFmt().mm())
                    )
                    playerGuiOrigin.remove(player.uniqueId)
                    task.cancel()
                }
            },
            0.ticks,
            1.ticks
        )
    }

    /**
     * Creates visual and auditory effects at the specified location, simulating a teleportation event.
     * @param location The location where the teleportation effects (particles and sound) will be generated.
     */
    private fun teleportEffects(location: Location) {
        location.world.spawnParticle(Particle.PORTAL, location, 60, 0.5, 1.0, 0.5, 0.2)
        location.world.spawnParticle(Particle.END_ROD, location, 20, 0.2, 0.8, 0.2, 0.05)
        location.world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f)
    }

    private fun gui(): Unit = TODO()
}