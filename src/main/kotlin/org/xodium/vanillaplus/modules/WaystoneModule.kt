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
import org.xodium.vanillaplus.utils.FmtUtils.mm
import org.xodium.vanillaplus.utils.TimeUtils.seconds
import org.xodium.vanillaplus.utils.TimeUtils.ticks
import org.xodium.vanillaplus.utils.Utils
import java.util.*
import kotlin.uuid.ExperimentalUuidApi

//TODO: Optional, do we add that you have to discover waypoints manually first before being able to use them?
//TODO: Add waystone custom texture.
//TODO: Check if the Database can handle serialization/deserialization of NamespacedKey.

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

    private val waystones = mutableMapOf<NamespacedKey, WaystoneData>()
    private val originWaystone = mutableMapOf<UUID, Location>()

    init {
        if (enabled()) {
            Database.createTable(this::class)
            val waystones =
                (Database.getData(this::class) as? List<*>)?.filterIsInstance<WaystoneData>() ?: emptyList()
            for (waystone in waystones) this@WaystoneModule.waystones[waystone.id] = waystone
            instance.server.addRecipe(WaystoneData.recipe(WaystoneData.item("")))
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
        if (itemMeta != null && itemMeta.hasCustomModelData() && itemMeta.customModelData == Config.WaystoneModule.WAYSTONE_CUSTOM_MODEL_DATA) {
            val waystone = WaystoneData(customName = itemMeta.displayName().toString(), location = event.block.location)
            Database.setData(this::class, waystone.id.toString(), waystone.toString())
            waystones[waystone.id] = waystone
            waystoneCreateEffect(event.block.location)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockBreakEvent) {
        val waystones = this@WaystoneModule.waystones.entries.find { it.value.location == event.block.location }
        if (event.block.type == Material.STONE_BRICKS && waystones != null) {
            this@WaystoneModule.waystones.remove(waystones.key)
            Database.deleteData(this::class, waystones.value.id.toString())
            waystoneDeleteEffect(event.block.location)
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        if (block.type == Material.STONE_BRICKS && waystones.any { it.value.location == block.location }) {
            event.isCancelled = true
            originWaystone[event.player.uniqueId] = block.location
            TODO("open gui")
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        event.isCancelled = true
        val slot = event.rawSlot

        val originLoc = originWaystone[player.uniqueId]
        val filteredWaystones = waystones.values.filter { it.location != originLoc }.toList()

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

                ticks++
                if (ticks >= delayTicks) {
                    if (player.gameMode in listOf(GameMode.SURVIVAL, GameMode.ADVENTURE)) {
                        Utils.chargePlayerXp(player, xpCost)
                    }
                    teleportEffect(player.location)
                    player.teleport(targetData.location)
                    teleportEffect(targetData.location)
                    task.cancel()
                } else {
                    teleportInitEffect(player.location)
                }
            },
            0.ticks,
            1.ticks
        )
    }

    /**
     * Creates an initial visual and auditory effect at the specified location, simulating the start of a teleportation process.
     *
     * @param location The location where the teleportation initiation effects (particles and sound) will be generated.
     */
    private fun teleportInitEffect(location: Location) {
        location.world.spawnParticle(Particle.PORTAL, location.add(0.0, 1.0, 0.0), 20, 0.5, 1.0, 0.5, 0.1)
        location.world.playSound(location, Sound.BLOCK_NOTE_BLOCK_HAT, 0.2f, 2.0f)
    }

    /**
     * Creates visual and auditory effects at the specified location, simulating a teleportation event.
     * @param location The location where the teleportation effects (particles and sound) will be generated.
     */
    private fun teleportEffect(location: Location) {
        location.world.spawnParticle(Particle.PORTAL, location, 60, 0.5, 1.0, 0.5, 0.2)
        location.world.spawnParticle(Particle.END_ROD, location, 20, 0.2, 0.8, 0.2, 0.05)
        location.world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f)
    }

    /**
     * Creates a visual and auditory effect at the specified location, simulating the activation of a waystone.
     *
     * @param location The location where the waystone activation effects (particles and sound) will be generated.
     */
    private fun waystoneCreateEffect(location: Location) {
        val centerLoc = location.clone().add(0.5, 1.0, 0.5)
        location.world.spawnParticle(Particle.END_ROD, centerLoc, 30, 0.3, 0.8, 0.3, 0.05)
        location.world.spawnParticle(Particle.PORTAL, centerLoc, 40, 0.3, 0.5, 0.3, 0.1)
        location.world.playSound(centerLoc, Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 1.0f, 1.2f)
        location.world.players.forEach { player ->
            if (player.location.distance(centerLoc) <= 30) {
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.PLAYERS, 0.5f, 1.0f)
            }
        }
    }

    /**
     * Creates visual and auditory effects at the specified location, simulating the deletion of a waystone.
     *
     * @param location The location where the waystone deletion effects (particles and sound) will be generated.
     */
    private fun waystoneDeleteEffect(location: Location) {
        val centerLoc = location.clone().add(0.5, 1.0, 0.5)
        location.world.spawnParticle(Particle.SMOKE, centerLoc, 50, 0.4, 0.8, 0.4, 0.05)
        location.world.spawnParticle(Particle.EXPLOSION, centerLoc, 3, 0.2, 0.2, 0.2, 0.0)
        location.world.spawnParticle(Particle.REVERSE_PORTAL, centerLoc, 30, 0.3, 0.5, 0.3, 0.05)
        location.world.playSound(centerLoc, Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 1.0f, 0.8f)
        location.world.playSound(centerLoc, Sound.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 0.6f, 0.8f)
        location.world.players.forEach { player ->
            if (player.location.distance(centerLoc) <= 30) {
                player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, SoundCategory.PLAYERS, 0.5f, 0.8f)
            }
        }
    }

    @Suppress("unused")
    private fun gui(): Unit = TODO("Use library for GUI")
}