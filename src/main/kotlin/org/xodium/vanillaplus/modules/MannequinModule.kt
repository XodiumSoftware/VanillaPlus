@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.entity.LookAnchor
import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.entity.Wolf
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.PlayerInventory
import org.bukkit.util.Vector
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.dialogs.MannequinDialog.dialog
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.pdcs.MannequinPDC.following
import org.xodium.vanillaplus.pdcs.MannequinPDC.owner
import org.xodium.vanillaplus.pdcs.MannequinPDC.proxyId
import org.xodium.vanillaplus.utils.Utils.MM
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Represents a module handling mannequin mechanics within the system. */
internal object MannequinModule : ModuleInterface {
    private val lastOwnerLocations = mutableMapOf<UUID, Location>()
    private val pendingArmorCancel = mutableSetOf<UUID>()

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
                } else if (player.inventory.itemInMainHand.type == Material.AIR) {
                    val slot = event.clickedPosition.toArmorSlot(entity) ?: return
                    removeArmor(entity, player, slot)
                } else if (player.inventory.itemInMainHand.type == config.mannequinModule.followTriggerItem) {
                    toggleFollow(entity, player)
                } else {
                    val slot =
                        player.inventory.itemInMainHand.type
                            .toArmorSlot()
                            ?: event.clickedPosition.toArmorSlot(entity)?.takeIf {
                                it == EquipmentSlot.HAND || it == EquipmentSlot.OFF_HAND
                            } ?: return
                    equipArmor(entity, player, slot)
                }
            }

            is Villager -> {
                if (player.inventory.itemInMainHand.type != config.mannequinModule.conversionTriggerItem) return

                consumeItem(player.inventory)
                villagerToMannequin(player, entity)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: PlayerInteractEvent) {
        if (pendingArmorCancel.remove(event.player.uniqueId)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun on(event: EntityDeathEvent) {
        val mannequin = event.entity as? Mannequin? ?: return

        mannequin.proxyId?.let { instance.server.getEntity(it)?.remove() }
        event.drops.apply {
            clear()
            addAll(listOf(*mannequin.equipment.armorContents))
            add(mannequin.equipment.itemInMainHand)
            add(mannequin.equipment.itemInOffHand)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: EntityDamageByEntityEvent) {
        val mannequin = event.entity as? Mannequin ?: return
        val attacker = event.damager as? Player ?: return

        mannequin.proxyId
            ?.let { instance.server.getEntity(it) as? Wolf }
            ?.takeIf { !it.isDead }
            ?.apply {
                isAngry = true
                target = attacker
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

    /** Maps this [Material] to the [EquipmentSlot] it occupies when worn, or null if it is not wearable armor. */
    private fun Material.toArmorSlot(): EquipmentSlot? =
        when {
            Tag.ITEMS_HEAD_ARMOR.isTagged(this) -> EquipmentSlot.HEAD
            Tag.ITEMS_CHEST_ARMOR.isTagged(this) -> EquipmentSlot.CHEST
            Tag.ITEMS_LEG_ARMOR.isTagged(this) -> EquipmentSlot.LEGS
            Tag.ITEMS_FOOT_ARMOR.isTagged(this) -> EquipmentSlot.FEET
            else -> null
        }

    /**
     * Maps a click position (relative to entity feet) to the [EquipmentSlot] at that location on a ~1.8-tall mannequin.
     * Clicks outside the body width (~0.3) at arm height are resolved to [EquipmentSlot.HAND] or
     * [EquipmentSlot.OFF_HAND] by projecting the click onto the entity's local right axis.
     * Returns null if the position is out of range.
     */
    private fun Vector.toArmorSlot(entity: Mannequin): EquipmentSlot? {
        val yawRad = Math.toRadians(entity.location.yaw.toDouble())
        // Project the horizontal click offset onto the entity's local right axis.
        // Right direction = (-cos(yaw), -sin(yaw)) in world (x, z).
        val localX = -(x * cos(yawRad) + z * sin(yawRad))

        if (y in 0.75..1.5 && abs(localX) > 0.1) {
            return if (localX > 0) EquipmentSlot.HAND else EquipmentSlot.OFF_HAND
        }

        return when {
            y >= 1.2 -> EquipmentSlot.HEAD
            y >= 0.75 -> EquipmentSlot.CHEST
            y >= 0.35 -> EquipmentSlot.LEGS
            y >= 0.0 -> EquipmentSlot.FEET
            else -> null
        }
    }

    /**
     * Swaps the item [player] is holding into [slot] on [mannequin], returning whatever was there to the player's hand.
     * @param mannequin The mannequin to equip.
     * @param player The player performing the action.
     * @param slot The armor slot to modify.
     */
    private fun equipArmor(
        mannequin: Mannequin,
        player: Player,
        slot: EquipmentSlot,
    ) {
        val newItem = player.inventory.itemInMainHand.clone()
        val oldItem = mannequin.equipment.getItem(slot)

        mannequin.equipment.setItem(slot, newItem)
        player.inventory.setItemInMainHand(oldItem)
        pendingArmorCancel += player.uniqueId
    }

    /**
     * Removes the item in [slot] from [mannequin] and gives it to [player].
     * Does nothing if the slot is empty.
     * @param mannequin The mannequin to unequip.
     * @param player The player receiving the item.
     * @param slot The armor slot to clear.
     */
    private fun removeArmor(
        mannequin: Mannequin,
        player: Player,
        slot: EquipmentSlot,
    ) {
        val item = mannequin.equipment.getItem(slot)

        if (item.type == Material.AIR) return

        mannequin.equipment.setItem(slot, null)
        player.inventory.addItem(item).forEach { (_, leftover) ->
            player.world.dropItemNaturally(player.location, leftover)
        }
        pendingArmorCancel += player.uniqueId
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
        instance.server.worlds
            .flatMap { it.entities }
            .filterIsInstance<Mannequin>()
            .filter { it.following }
            .forEach { mannequin ->
                mannequin.proxyId
                    ?.let { instance.server.getEntity(it) as? Wolf }
                    ?.takeIf { !it.isDead }
                    ?.let { mannequin.teleport(it.location) }
            }
    }

    /** Updates pathfinding for all following mannequins, only re-pathing when the owner moves or proxy has no path. */
    private fun updateFollowMovement() {
        val stopDistSq = config.mannequinModule.followStopDistance * config.mannequinModule.followStopDistance

        instance.server.worlds
            .flatMap { it.entities }
            .filterIsInstance<Mannequin>()
            .filter { it.following }
            .forEach { mannequin ->
                val owner = instance.server.getPlayer(mannequin.owner) ?: return@forEach

                if (owner.world != mannequin.world) return@forEach

                val proxy =
                    mannequin.proxyId
                        ?.let { instance.server.getEntity(it) as? Wolf }
                        ?.takeIf { !it.isDead }
                        ?: spawnProxy(mannequin).also { mannequin.proxyId = it.uniqueId }
                val ownerLoc = owner.location

                if (proxy.location.distanceSquared(ownerLoc) > stopDistSq) {
                    val lastLoc = lastOwnerLocations[mannequin.uniqueId]

                    if (!proxy.pathfinder.hasPath() || lastLoc == null || lastLoc.distanceSquared(ownerLoc) > 1.0) {
                        proxy.pathfinder.moveTo(ownerLoc, config.mannequinModule.followSpeed)
                        lastOwnerLocations[mannequin.uniqueId] = ownerLoc.clone()
                    }
                } else {
                    proxy.pathfinder.stopPathfinding()
                }
            }
    }

    /**
     * Spawns an invisible, silent, invulnerable [Wolf] at [mannequin]'s location to act as a pathfinding proxy.
     * Being a neutral mob, the wolf will attack players who damage the mannequin.
     * @param mannequin The mannequin that will follow this proxy.
     * @return The spawned proxy [Wolf].
     */
    private fun spawnProxy(mannequin: Mannequin): Wolf =
        (mannequin.world.spawnEntity(mannequin.location, EntityType.WOLF) as Wolf).apply {
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
        instance.server.worlds
            .flatMap { it.entities }
            .filterIsInstance<Mannequin>()
            .forEach { mannequin ->
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
        var enabled: Boolean = true,
        var conversionTriggerItem: Material = Material.TOTEM_OF_UNDYING,
        var lookRange: Double = 10.0,
        var followTriggerItem: Material = Material.FEATHER,
        var followSpeed: Double = 1.0,
        var followStopDistance: Double = 2.5,
    )
}
