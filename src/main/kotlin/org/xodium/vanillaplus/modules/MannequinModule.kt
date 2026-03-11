@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.entity.LookAnchor
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Pig
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.world.EntitiesLoadEvent
import org.bukkit.event.world.EntitiesUnloadEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.PlayerInventory
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.dialogs.MannequinDialog.dialog
import org.xodium.vanillaplus.interfaces.ModuleConfigInterface
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.menus.MannequinEquipmentMenu.handleMannequinMenuClicking
import org.xodium.vanillaplus.pdcs.MannequinPDC.following
import org.xodium.vanillaplus.pdcs.MannequinPDC.owner
import org.xodium.vanillaplus.pdcs.MannequinPDC.proxyId
import org.xodium.vanillaplus.utils.Utils.MM
import java.util.*

/** Represents a module handling mannequin mechanics within the system. */
internal object MannequinModule : ModuleInterface {
    override val moduleConfig get() = config.mannequinModule

    private val trackedMannequins = mutableSetOf<Mannequin>()
    private val lastOwnerLocations = mutableMapOf<UUID, Location>()
    private val lastLookUpdateTick = mutableMapOf<UUID, Int>()

    init {
        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable { syncMannequinPositions() },
            0L,
            1L,
        )
        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable {
                updateFollowMovement()
                updateHeadMovement()
            },
            0L,
            5L,
        )
    }

    @EventHandler
    fun on(event: InventoryClickEvent) = handleMannequinMenuClicking(event)

    @EventHandler
    fun on(event: EntitySpawnEvent) {
        val mannequin = event.entity as? Mannequin ?: return
        trackedMannequins.add(mannequin)
    }

    @EventHandler
    fun on(event: EntitiesLoadEvent) {
        event.entities.filterIsInstanceTo(trackedMannequins)
    }

    @EventHandler
    fun on(event: EntitiesUnloadEvent) {
        event.entities.filterIsInstance<Mannequin>().forEach { mannequin ->
            trackedMannequins.remove(mannequin)
            lastOwnerLocations.remove(mannequin.uniqueId)
            lastLookUpdateTick.remove(mannequin.uniqueId)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerInteractAtEntityEvent) {
        val player = event.player

        when (val entity = event.rightClicked) {
            is Mannequin -> {
                if (event.hand != EquipmentSlot.HAND) return
                if (entity.owner != player.uniqueId) return

                event.isCancelled = true

                if (player.isSneaking) {
                    player.showDialog(entity.dialog())
                } else if (player.inventory.itemInMainHand.type == config.mannequinModule.followTriggerItem) {
                    toggleFollow(entity, player)
                }
            }

            is Villager -> {
                if (player.inventory.itemInMainHand.type != config.mannequinModule.conversionTriggerItem) return

                consumeItem(player.inventory)
                villagerToMannequin(player, entity)
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun on(event: EntityDeathEvent) {
        val mannequin = event.entity as? Mannequin? ?: return

        mannequin.proxyId?.let { instance.server.getEntity(it)?.remove() }
        trackedMannequins.remove(mannequin)
        lastOwnerLocations.remove(mannequin.uniqueId)
        lastLookUpdateTick.remove(mannequin.uniqueId)
        event.drops.apply {
            clear()
            addAll(listOf(*mannequin.equipment.armorContents))
            add(mannequin.equipment.itemInMainHand)
            add(mannequin.equipment.itemInOffHand)
        }
    }

    /**
     * Converts the interacted villager into a mannequin entity.
     * @param villager The villager to convert.
     */
    private fun villagerToMannequin(
        player: Player,
        villager: Villager,
    ) {
        val mannequin = villager.world.spawnEntity(villager.location, EntityType.MANNEQUIN) as Mannequin

        mannequin.customName(villager.customName())
        mannequin.owner = player.uniqueId

        villager.remove()
    }

    /**
     * Consumes one instance of the configured trigger item from the player's main hand.
     * @param inventory The player's inventory.
     */
    private fun consumeItem(inventory: PlayerInventory) {
        val item = inventory.itemInMainHand

        if (item.amount <= 1) {
            inventory.setItemInMainHand(null)
            return
        }

        item.amount -= 1
        inventory.setItemInMainHand(item)
    }

    /** Toggles follow mode on [mannequin], spawning or removing the proxy mob as needed, and notifies [player]. */
    private fun toggleFollow(
        mannequin: Mannequin,
        player: Player,
    ) {
        if (mannequin.following) {
            mannequin.proxyId?.let { instance.server.getEntity(it)?.remove() }
            mannequin.proxyId = null
            mannequin.following = false
        } else {
            mannequin.following = true
        }
        player.sendActionBar(
            MM.deserialize(
                if (mannequin.following) {
                    "<gray>Following: <green>ON"
                } else {
                    "<gray>Following: <red>OFF"
                },
            ),
        )
    }

    /** Teleports all following mannequins to their proxy's current position every tick for smooth movement. */
    private fun syncMannequinPositions() {
        trackedMannequins
            .filter { it.following }
            .forEach { mannequin ->
                mannequin.proxyId
                    ?.let { instance.server.getEntity(it) as? Pig }
                    ?.takeIf { !it.isDead }
                    ?.let { mannequin.teleport(it.location) }
            }
    }

    /** Updates pathfinding for all following mannequins, only re-pathing when the owner moves or proxy has no path. */
    private fun updateFollowMovement() {
        val stopDistSq = config.mannequinModule.followStopDistance * config.mannequinModule.followStopDistance

        trackedMannequins
            .filter { it.following }
            .forEach { mannequin ->
                val owner = instance.server.getPlayer(mannequin.owner) ?: return@forEach

                if (owner.world != mannequin.world) return@forEach

                val proxy =
                    mannequin.proxyId
                        ?.let { instance.server.getEntity(it) as? Pig }
                        ?.takeIf { !it.isDead }
                        ?: spawnProxy(mannequin).also { mannequin.proxyId = it.uniqueId }

                val ownerLoc = owner.location

                val distSq = proxy.location.distanceSquared(ownerLoc)

                if (distSq > stopDistSq) {
                    val toOwner = ownerLoc.toVector().subtract(proxy.eyeLocation.toVector())
                    val hasLos =
                        proxy.world.rayTraceBlocks(proxy.eyeLocation, toOwner.normalize(), toOwner.length()) == null
                    val maxDist = config.mannequinModule.followMaxDistance

                    if (distSq > maxDist * maxDist || !hasLos) {
                        if (!proxy.pathfinder.hasPath()) {
                            proxy.remove()
                            mannequin.proxyId = null
                            lastOwnerLocations.remove(mannequin.uniqueId)
                        }
                        return@forEach
                    }

                    val lastLoc = lastOwnerLocations[mannequin.uniqueId]

                    if (!proxy.pathfinder.hasPath() || lastLoc == null || lastLoc.distanceSquared(ownerLoc) > 1.0) {
                        proxy.pathfinder.moveTo(ownerLoc, config.mannequinModule.followSpeed)
                        lastOwnerLocations[mannequin.uniqueId] = ownerLoc.clone()
                    }
                } else {
                    proxy.remove()
                    mannequin.proxyId = null
                    lastOwnerLocations.remove(mannequin.uniqueId)
                }
            }
    }

    /**
     * Spawns an invisible, silent, invulnerable [Pig] at [mannequin]'s location to act as a pathfinding proxy.
     * @param mannequin The mannequin that will follow this proxy.
     * @return The spawned proxy [Pig].
     */
    private fun spawnProxy(mannequin: Mannequin): Pig =
        (mannequin.world.spawnEntity(mannequin.location, EntityType.PIG) as Pig).apply {
            isInvisible = true
            isSilent = true
            isInvulnerable = true
            isPersistent = true
            isCollidable = false
            addScoreboardTag("vanillaplus:mannequin_proxy")
        }

    /** Updates the head rotation of all mannequins in the world. */
    @Suppress("UnstableApiUsage")
    private fun updateHeadMovement() {
        val tick = instance.server.currentTick
        val interval = config.mannequinModule.lookUpdateInterval

        trackedMannequins
            .forEach { mannequin ->
                if (tick - (lastLookUpdateTick[mannequin.uniqueId] ?: 0) < interval) return@forEach

                lastLookUpdateTick[mannequin.uniqueId] = tick

                val eyeLoc = mannequin.eyeLocation

                mannequin.world.players
                    .filter {
                        it.location.distanceSquared(mannequin.location) <=
                            config.mannequinModule.lookRange * config.mannequinModule.lookRange
                    }.sortedBy { it.location.distanceSquared(mannequin.location) }
                    .firstOrNull {
                        val toPlayer = it.eyeLocation.toVector().subtract(eyeLoc.toVector())
                        val dist = toPlayer.length()

                        mannequin.world.rayTraceBlocks(eyeLoc, toPlayer.normalize(), dist) == null
                    }?.let { mannequin.lookAt(it.eyeLocation, LookAnchor.EYES) }
            }
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        override var enabled: Boolean = false,
        var conversionTriggerItem: Material = Material.TOTEM_OF_UNDYING,
        var lookRange: Double = 10.0,
        var lookUpdateInterval: Int = 10,
        var followTriggerItem: Material = Material.FEATHER,
        var followSpeed: Double = 1.0,
        var followStopDistance: Double = 2.5,
        var followMaxDistance: Double = 32.0,
    ) : ModuleConfigInterface
}
