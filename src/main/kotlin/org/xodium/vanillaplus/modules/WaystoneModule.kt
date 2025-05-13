/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import dev.triumphteam.gui.paper.kotlin.builder.chestContainer
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.entity.TeleportFlag
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.PlayerData
import org.xodium.vanillaplus.data.WaystoneData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.cmd
import org.xodium.vanillaplus.utils.ExtUtils.il
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.Utils
import java.util.*
import kotlin.time.Duration.Companion.seconds

/** Represents a module handling waystone mechanics within the system. */
class WaystoneModule : ModuleInterface {
    override fun enabled(): Boolean = config.ENABLED

    private val config = Config.WaystoneModule
    private val waystones = mutableListOf<WaystoneData>()
    private val originWaystone = mutableMapOf<UUID, Location>()
    private val playerDiscoveryData = mutableMapOf<UUID, PlayerData>()
    private val guiTitle = "Waystones Index".fireFmt()
    private val allowedGameModes = listOf(GameMode.SURVIVAL, GameMode.ADVENTURE)

    init {
        if (enabled()) {
            WaystoneData.createTable()
            PlayerData.createTable()
            waystones.addAll(WaystoneData.getData())
            instance.server.onlinePlayers.forEach { loadPlayerData(it) }
            instance.server.addRecipe(recipe(item()))
        }
    }

    @Suppress("UnstableApiUsage")
    override fun cmd(): LiteralArgumentBuilder<CommandSourceStack>? {
        return Commands.literal("waystone")
            .requires { it.sender.hasPermission(Perms.Waystone.USE) }
            .executes { it ->
                Utils.tryCatch(it) {
                    (it.sender as Player).inventory.addItem(item())
                }
            }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return
        loadPlayerData(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerQuitEvent) {
        if (!enabled()) return
        playerDiscoveryData.remove(event.player.uniqueId)
        originWaystone.remove(event.player.uniqueId)
    }

    @Suppress("UnstableApiUsage")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockPlaceEvent) {
        if (!enabled()) return
        val item = event.itemInHand
        if (item.hasData(DataComponentTypes.CUSTOM_MODEL_DATA)) {
            val customModelData = item.getData(DataComponentTypes.CUSTOM_MODEL_DATA)
            if (customModelData?.strings()?.contains(config.WAYSTONE_CUSTOM_MODEL_DATA) == true) {
                val customName = item.getData(DataComponentTypes.CUSTOM_NAME) ?: "Waystone".mm()
                val plainText = PlainTextComponentSerializer.plainText().serialize(customName)
                val waystone = WaystoneData(customName = plainText, location = event.block.location)
                WaystoneData.setData(waystone)
                waystones.add(waystone)
                waystoneCreateEffect(event.block.location)
                discoverWaystone(event.player, waystone)
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: BlockBreakEvent) {
        if (!enabled()) return
        val waystone = waystones.find { it.location == event.block.location }
        if (event.block.type == config.WAYSTONE_MATERIAL && waystone != null) {
            waystones.remove(waystone)
            WaystoneData.deleteData(waystone.id)
            playerDiscoveryData.values.forEach { playerData ->
                if (playerData.discoveredWaystones?.contains(waystone.id) == true) {
                    val updatedList = playerData.discoveredWaystones.toMutableList().apply { remove(waystone.id) }
                    val updatedData = playerData.copy(discoveredWaystones = updatedList)
                    playerDiscoveryData[UUID.fromString(playerData.id)] = updatedData
                    PlayerData.setData(updatedData)
                }
            }
            waystoneDeleteEffect(event.block.location)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (!enabled()) return
        if (event.action != Action.RIGHT_CLICK_BLOCK || event.player.isSneaking) return
        val block = event.clickedBlock ?: return
        val waystone = waystones.find { it.location == block.location }
        if (block.type == config.WAYSTONE_MATERIAL && waystones.any { it.location == block.location }) {
            event.isCancelled = true
            val player = event.player
            val playerData = getPlayerData(player)
            if (playerData.discoveredWaystones?.contains(waystone?.id) == true) {
                originWaystone[player.uniqueId] = block.location
                gui(player).open(player)
            } else {
                discoverWaystone(player, waystone ?: return)
            }
            originWaystone[event.player.uniqueId] = block.location
            gui(event.player).open(event.player)
        }
    }

    /**
     * Adds a waystone to the player's discovered list and saves the data.
     * Notifies the player about the discovery.
     * @param player The player discovering the waystone.
     * @param waystone The waystone being discovered.
     */
    private fun discoverWaystone(player: Player, waystone: WaystoneData) {
        val playerData = getPlayerData(player)
        val discoveredList = playerData.discoveredWaystones?.toMutableList() ?: mutableListOf()

        if (!discoveredList.contains(waystone.id)) {
            discoveredList.add(waystone.id)
            val updatedData = playerData.copy(discoveredWaystones = discoveredList)
            playerDiscoveryData[player.uniqueId] = updatedData
            PlayerData.setData(updatedData)
        }
    }

    /**
     * Loads player data from the database into the memory map. Creates default data if none exists.
     * @param player The player whose data needs to be loaded.
     */
    private fun loadPlayerData(player: Player) {
        val uuid = player.uniqueId
        if (!playerDiscoveryData.containsKey(uuid)) {
            val existingData = PlayerData.getData().find { it.id == uuid.toString() }
            if (existingData != null) {
                playerDiscoveryData[uuid] = existingData
            } else {
                val newData = PlayerData(id = uuid.toString(), discoveredWaystones = mutableListOf())
                playerDiscoveryData[uuid] = newData
                PlayerData.setData(newData)
            }
        }
    }

    /**
     * Retrieves PlayerData for a given player from the memory map, loading if necessary.
     * @param player The player whose data is needed.
     * @return The PlayerData associated with the player.
     */
    private fun getPlayerData(player: Player): PlayerData {
        return playerDiscoveryData[player.uniqueId] ?: run {
            loadPlayerData(player)
            playerDiscoveryData[player.uniqueId]!!
        }
    }

    /**
     * Creates an initial visual and auditory effect at the specified location, simulating the start of a teleportation process.
     * @param location The location where the teleportation initiation effects (particles and sound) will be generated.
     */
    private fun teleportInitEffect(location: Location) {
        location.world.spawnParticle(Particle.PORTAL, location.add(0.0, 1.0, 0.0), 20, 0.5, 1.0, 0.5, 0.2)
        location.world.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 0.2f, 2.0f)
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
     * @param player The player who is attempting to teleport.
     * @param targetWaystone The target waystone data containing the destination location for teleportation.
     */
    private fun handleTeleportation(player: Player, targetWaystone: WaystoneData) {
        val originLoc = originWaystone[player.uniqueId] ?: return
        val xpCost = calculateXpCost(originLoc, targetWaystone.location, player.isInsideVehicle)

        if (player.gameMode in allowedGameModes) {
            val playerTotalXp = player.level * 7 + player.exp.toInt()
            if (playerTotalXp < xpCost) {
                player.closeInventory()
                return player.sendActionBar("You need $xpCost XP to teleport to this waystone".fireFmt().mm())
            }
        }

        player.closeInventory()

        val initialLocation = player.location.clone()
        val mountedEntity = player.vehicle
        val delayTicks = 3L * 20L

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
                    if (player.gameMode in allowedGameModes) chargePlayerXp(player, xpCost)

                    teleportEffect(player.location)

                    val targetLocation = getTeleportLocationNextTo(targetWaystone.location, mountedEntity != null)
                    if (mountedEntity != null) {
                        mountedEntity.teleportAsync(
                            targetLocation,
                            TeleportCause.PLUGIN,
                            TeleportFlag.EntityState.RETAIN_PASSENGERS
                        )
                    } else {
                        player.teleportAsync(targetLocation)
                    }

                    teleportEffect(targetLocation)
                    return@runTaskTimer task.cancel()
                } else {
                    teleportInitEffect(player.location)
                }
            },
            0L, 1L
        )
    }

    /**
     * Charges the player the specified amount of XP.
     * @param player The player to charge.
     * @param amount The amount of XP to charge.
     */
    private fun chargePlayerXp(player: Player, amount: Int): Player {
        return player.apply {
            val remainingXp = maxOf(0, totalExperience - amount)
            totalExperience = 0
            level = 0
            exp = 0f
            if (remainingXp > 0) giveExp(remainingXp)
        }
    }

    /**
     * Determines a valid teleportation location near the given location.
     * @param location The initial location from which to search for adjacent teleportation spots.
     * @param withMount A boolean indicating whether the teleportation location should have enough space
     *                  to accommodate a mount.
     * @return The determined teleportation location near the given location. If no valid spot is found,
     *         the original location is returned.
     */
    private fun getTeleportLocationNextTo(location: Location, withMount: Boolean): Location {
        val world = location.world ?: return location
        val x = location.blockX
        val y = location.blockY
        val z = location.blockZ
        val offsetDist = if (withMount) 2 else 1
        val offsets = listOf(
            0 to offsetDist, offsetDist to 0, 0 to -offsetDist, -offsetDist to 0,
            offsetDist to offsetDist, offsetDist to -offsetDist, -offsetDist to offsetDist, -offsetDist to -offsetDist
        )
        val requiredClearance = if (withMount) 3 else 2
        for ((dx, dz) in offsets) {
            val tx = x + dx
            val tz = z + dz
            val ground = world.getBlockAt(tx, y - 1, tz)
            if (!ground.type.isSolid || ground.type == Material.BEDROCK) continue

            var blocked = false
            for (dy in 0..<requiredClearance) {
                val block = world.getBlockAt(tx, y + dy, tz)
                if (!block.type.isAir && !block.isPassable) {
                    blocked = true
                    break
                }
            }
            if (blocked) continue

            return Location(world, tx + 0.5, y.toDouble(), tz + 0.5, location.yaw, location.pitch)
        }
        return location
    }

    /**
     * Creates an `ItemStack` instance configured as a waystone item, with optional origin and destination data.
     *
     * If both origin and destination are provided, the item will include a lore displaying the XP cost
     * required for travelling between the two locations.
     * @param customName The custom name of the item. Defaults to "Waystone".
     * @param origin The origin `WaystoneData` representing the starting location. Null if not needed.
     * @param destination The destination `WaystoneData` representing the target location. Null if not needed.
     * @param player The player interacting with the waystone, used to determine if the player is mounted.
     * @return A configured `ItemStack` representing the waystone with the specified attributes and lore.
     */
    private fun item(
        customName: String = "Waystone",
        origin: Location? = null,
        destination: Location? = null,
        player: Player? = null,
    ): ItemStack {
        val loreLines = mutableListOf("Click to teleport".fireFmt())
        if (origin != null && destination != null && player?.gameMode in allowedGameModes) {
            loreLines.add(
                "Travel Cost: ${
                    calculateXpCost(
                        origin,
                        destination,
                        player?.isInsideVehicle ?: false
                    )
                }"
            )
        }
        @Suppress("UnstableApiUsage")
        return ItemStack.of(config.WAYSTONE_MATERIAL).apply {
            setData(DataComponentTypes.CUSTOM_NAME, customName.mm())
            setData(DataComponentTypes.CUSTOM_MODEL_DATA, config.WAYSTONE_CUSTOM_MODEL_DATA.cmd())
            setData(DataComponentTypes.LORE, loreLines.il())
        }
    }

    /**
     * Calculates the experience point (XP) cost for travelling between two locations,
     * factoring in whether the travel is mounted and whether the destination is in a different dimension.
     * @param origin The starting location of the player, represented as a `Location` object.
     * @param destination The destination location of the player, represented as a `Location` object.
     * @param isMounted A Boolean flag indicating whether the player is mounted (e.g. riding a horse). Default is false.
     * @return The calculated XP cost as an integer based on the distance, dimension, and whether the player is mounted.
     */
    private fun calculateXpCost(origin: Location, destination: Location, isMounted: Boolean): Int {
        val baseCost = config.BASE_XP_COST + when (origin.world) {
            destination.world -> (origin.distance(destination) * config.DISTANCE_MULTIPLIER).toInt()
            else -> config.DIMENSIONAL_MULTIPLIER
        }
        return if (isMounted) (baseCost * config.MOUNT_MULTIPLIER).toInt() else baseCost
    }

    /**
     * Creates a custom-shaped crafting recipe for the given item.
     * @param itemStack Representing the item for which the recipe is created.
     * @return A custom `ShapedRecipe` for the provided item using the defined shape and ingredients.
     */
    private fun recipe(itemStack: ItemStack): Recipe {
        return ShapedRecipe(NamespacedKey(instance, "waystone_recipe"), itemStack).apply {
            shape("CCC", "CBC", "AAA")
            setIngredient('A', Material.OBSIDIAN)
            setIngredient('B', Material.ENDER_EYE)
            setIngredient('C', Material.GLASS)
        }
    }

    /**
     * Creates a graphical user interface (GUI) for the player, allowing them to interact with and teleport to waystones.
     *
     * The GUI is dynamically generated based on the filtered list of available waystones, excludes the player's current waystone
     * (if any), and displays relevant information such as the waystone's name and teleportation cost.
     * @param player The player for whom the GUI is being generated.
     * @return A `Gui` instance representing the graphical user interface for waystone interaction.
     */
    private fun gui(player: Player): Gui {
        val discoveredIds = getPlayerData(player).discoveredWaystones ?: emptyList()
        val currentOrigin = originWaystone[player.uniqueId]
        val filteredWaystones = waystones.filter { waystone ->
            discoveredIds.contains(waystone.id) && waystone.location != currentOrigin
        }
        val waystoneCount = filteredWaystones.size
        val rows = ((waystoneCount + 8) / 9).coerceIn(1, 6)
        return buildGui {
            containerType = chestContainer { this.rows = rows }
            spamPreventionDuration = 1.seconds
            title(guiTitle.mm())
            statelessComponent { container ->
                filteredWaystones.forEachIndexed { index, waystone ->
                    val row = (index / 9) + 1
                    val col = (index % 9) + 1
                    currentOrigin?.let { originLoc ->
                        container[row, col] = ItemBuilder.from(
                            item(
                                waystone.customName,
                                originLoc,
                                waystone.location,
                                player
                            )
                        ).asGuiItem { _, _ -> handleTeleportation(player, waystone) }
                    }
                }
            }
        }
    }
}