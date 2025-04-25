/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import dev.triumphteam.gui.paper.kotlin.builder.chestContainer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.xodium.vanillaplus.Config
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

//TODO: Idea, do we add that you have to discover waypoints manually first before being able to use them?
//TODO: Teleporting with mount doesn't work without CMI.
//TODO: Idea, make it cost xp to place a waystone down, and maybe return xp when destroying waystone, half?.
//TODO: Add waystone custom texture.

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

    private val waystones = mutableListOf<WaystoneData>()
    private val originWaystone = mutableMapOf<UUID, Location>()
    private val guiTitle = "Waystones Teleportation Index".fireFmt()

    init {
        if (enabled()) {
            WaystoneData.createTable()
            waystones.addAll(WaystoneData.getData())
            instance.server.addRecipe(WaystoneData.recipe(WaystoneData.item()))
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockPlaceEvent) {
        val itemMeta = event.itemInHand.itemMeta
        if (itemMeta != null && itemMeta.hasCustomModelData() && itemMeta.customModelData == Config.WaystoneModule.WAYSTONE_CUSTOM_MODEL_DATA) {
            val plainText =
                PlainTextComponentSerializer.plainText().serialize(itemMeta.displayName() ?: "Waystone".mm())
            val waystone = WaystoneData(customName = plainText, location = event.block.location)
            WaystoneData.setData(waystone)
            waystones.add(waystone)
            waystoneCreateEffect(event.block.location)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockBreakEvent) {
        val waystone = waystones.find { it.location == event.block.location }
        if (event.block.type == Material.STONE_BRICKS && waystone != null) {
            waystones.remove(waystone)
            WaystoneData.deleteData(waystone.id)
            waystoneDeleteEffect(event.block.location)
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock ?: return
        if (block.type == Material.STONE_BRICKS && waystones.any { it.location == block.location }) {
            event.isCancelled = true
            originWaystone[event.player.uniqueId] = block.location
            gui(event.player).open(event.player)
        }
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

    /**
     * Handles the teleportation process of a player to a target waystone, including XP cost calculation,
     * teleportation effects, and cancellation conditions.
     *
     * @param player The player who is attempting to teleport.
     * @param targetWaystone The target waystone data containing the destination location for teleportation.
     */
    private fun handleTeleportation(player: Player, targetWaystone: WaystoneData) {
        val originLoc = originWaystone[player.uniqueId]
        val xpCost = WaystoneData.calculateXpCost(
            originLoc ?: player.location, targetWaystone.location, player.isInsideVehicle
        )

        if (player.gameMode in listOf(GameMode.SURVIVAL, GameMode.ADVENTURE)) {
            val playerTotalXp = player.level * 7 + player.exp.toInt()
            if (playerTotalXp < xpCost) {
                player.closeInventory()
                return player.sendActionBar("You need $xpCost XP to teleport to this waystone".fireFmt().mm())
            }
        }

        player.closeInventory()

        val initialLocation = player.location.clone()
        val mountedEntity = player.vehicle
        val delayTicks = 3.seconds

        var ticks = 0

        instance.server.scheduler.runTaskTimer(
            instance,
            { task ->
                val currentLocation = player.location
                val mountedLocation = mountedEntity?.location ?: player.location
                val tooFar = if (mountedEntity != null) {
                    mountedLocation.distanceSquared(initialLocation) > 4.0
                } else {
                    currentLocation.distanceSquared(initialLocation) > 0.5
                }

                if (!player.isOnline || player.isDead || tooFar) {
                    player.sendActionBar("Teleport cancelled!".fireFmt().mm())
                    return@runTaskTimer task.cancel()
                }

                ticks++
                if (ticks >= delayTicks) {
                    if (player.gameMode in listOf(GameMode.SURVIVAL, GameMode.ADVENTURE)) {
                        Utils.chargePlayerXp(player, xpCost)
                    }
                    teleportEffect(player.location)
                    val targetLocation = getTeleportLocationNextTo(targetWaystone.location)
                    mountedEntity?.teleport(targetLocation)
                    player.teleport(targetLocation)
                    teleportEffect(targetLocation)
                    return@runTaskTimer task.cancel()
                } else {
                    teleportInitEffect(player.location)
                }
            },
            0.ticks, 1.ticks
        )
    }

    /**
     * Finds a safe location adjacent to the target location in any horizontal direction.
     * @param location The original location (waystone center).
     * @return A safe location 1 block away horizontally, or the original location if no safe spots are found.
     */
    private fun getTeleportLocationNextTo(location: Location): Location {
        val offsets = listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))
        for ((xOffset, zOffset) in offsets) {
            val adjacentBlock = location.clone().add(xOffset.toDouble(), 0.0, zOffset.toDouble()).block
            if (adjacentBlock.type == Material.AIR) return adjacentBlock.location.add(0.5, 0.0, 0.5)
        }
        return location
    }


    /**
     * Creates a graphical user interface (GUI) for the player, allowing them to interact with and teleport to waystones.
     *
     * The GUI is dynamically generated based on the filtered list of available waystones, excludes the player's current waystone
     * (if any), and displays relevant information such as the waystone's name and teleportation cost.
     *
     * @param player The player for whom the GUI is being generated.
     * @return A `Gui` instance representing the graphical user interface for waystone interaction.
     */
    private fun gui(player: Player): Gui {
        val filteredWaystones = waystones.filter { it.location != originWaystone.values.firstOrNull() }
        val waystoneCount = filteredWaystones.size
        val rows = ((waystoneCount + 8) / 9).coerceIn(1, 6)
        return buildGui {
            containerType = chestContainer { this.rows = rows }
            title(guiTitle.mm())
            statelessComponent { container ->
                filteredWaystones.forEachIndexed { index, waystone ->
                    val row = (index / 9) + 1
                    val slot = (index % 9) + 1
                    container[row, slot] =
                        ItemBuilder.from(
                            WaystoneData.item(
                                waystone.customName,
                                originWaystone.values.first(),
                                waystone,
                                player
                            )
                        )
                            .asGuiItem { player, _ -> handleTeleportation(player, waystone) }
                }
            }
        }
    }
}