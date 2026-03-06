@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.entity.LookAnchor
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Pig
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.PlayerInventory
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.dialogs.MannequinDialog.dialog
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.pdcs.MannequinPDC.following
import org.xodium.vanillaplus.pdcs.MannequinPDC.owner
import org.xodium.vanillaplus.pdcs.MannequinPDC.proxyId
import org.xodium.vanillaplus.utils.Utils.MM

/** Represents a module handling mannequin mechanics within the system. */
internal object MannequinModule : ModuleInterface {
    init {
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
    fun on(event: PlayerInteractEntityEvent) {
        val player = event.player

        when (val entity = event.rightClicked) {
            is Mannequin -> {
                if (event.hand != EquipmentSlot.HAND) return
                if (entity.owner != player.uniqueId) return

                event.isCancelled = true
                if (player.isSneaking) {
                    player.showDialog(entity.dialog())
                } else {
                    toggleFollow(entity, player)
                }
            }

            is Villager -> {
                if (player.inventory.itemInMainHand.type != config.mannequinModule.triggerItem) return

                consumeItem(player.inventory)
                villagerToMannequin(player, entity)
            }
        }
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

    /** Moves all following mannequins toward their owner via an invisible proxy mob. */
    private fun updateFollowMovement() {
        instance.server.worlds
            .flatMap { it.entities }
            .filterIsInstance<Mannequin>()
            .filter { it.following }
            .forEach { mannequin ->
                val owner = instance.server.getPlayer(mannequin.owner) ?: return@forEach

                if (owner.world != mannequin.world) return@forEach

                val proxy =
                    mannequin.proxyId
                        ?.let { instance.server.getEntity(it) as? Pig }
                        ?.takeIf { !it.isDead }
                        ?: spawnProxy(mannequin).also { mannequin.proxyId = it.uniqueId }

                if (proxy.location.distanceSquared(owner.location) >
                    config.mannequinModule.followStopDistance * config.mannequinModule.followStopDistance
                ) {
                    proxy.pathfinder.moveTo(owner.location, config.mannequinModule.followSpeed)
                }

                mannequin.teleport(proxy.location)
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
        instance.server.worlds
            .flatMap { it.entities }
            .filterIsInstance<Mannequin>()
            .forEach { mannequin ->
                mannequin.world.players
                    .filter { it.location.distance(mannequin.location) <= config.mannequinModule.lookRange }
                    .minByOrNull { it.location.distanceSquared(mannequin.location) }
                    ?.let { mannequin.lookAt(it.eyeLocation, LookAnchor.EYES) }
            }
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var triggerItem: Material = Material.TOTEM_OF_UNDYING,
        var lookRange: Double = 10.0,
        var followSpeed: Double = 1.0,
        var followStopDistance: Double = 2.5,
    )
}
