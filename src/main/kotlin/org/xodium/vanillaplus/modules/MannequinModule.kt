@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.entity.LookAnchor
import kotlinx.serialization.Serializable
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mannequin
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.PlayerInventory
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.dialogs.MannequinDialog.dialog
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.pdcs.MannequinPDC.owner
import java.util.*

/** Represents a module handling mannequin mechanics within the system. */
internal object MannequinModule : ModuleInterface {
    private val trackingMannequins = mutableMapOf<UUID, Long>()
    private val mannequins = mutableListOf<Mannequin>()

    init {
        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable { updateHeadMovement() },
            0L,
            5L,
        )
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerInteractEntityEvent) {
        val player = event.player

        when (val entity = event.rightClicked) {
            is Mannequin -> {
                if (!player.isSneaking) return
                if (entity.owner != player.uniqueId) return

                player.showDialog(entity.dialog())
                event.isCancelled = true
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

        trackingMannequins.remove(mannequin.uniqueId)
        mannequins.removeIf { it.uniqueId == mannequin.uniqueId }
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

        trackingMannequins[mannequin.uniqueId] = System.currentTimeMillis()
        mannequins.add(mannequin)

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

    /** Updates the head rotation of all tracked mannequins. */
    private fun updateHeadMovement() {
        val iter = mannequins.iterator()

        while (iter.hasNext()) {
            val mannequin = iter.next()

            if (!mannequin.isValid || mannequin.isDead) {
                trackingMannequins.remove(mannequin.uniqueId)
                iter.remove()
                continue
            }

            val nearestPlayer = findNearestPlayerNearby(mannequin)

            @Suppress("UnstableApiUsage")
            if (nearestPlayer != null) mannequin.lookAt(nearestPlayer.eyeLocation, LookAnchor.EYES)
        }
    }

    /**
     * Finds the nearest player within the module's look range of the given mannequin.
     * @param mannequin The mannequin to check around.
     * @return The nearest player within range, or null if none are found.
     */
    private fun findNearestPlayerNearby(mannequin: Mannequin): Player? =
        mannequin.world.players
            .filter { it.location.distance(mannequin.location) <= config.mannequinModule.lookRange }
            .minByOrNull { it.location.distanceSquared(mannequin.location) }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var triggerItem: Material = Material.TOTEM_OF_UNDYING,
        var lookRange: Double = 10.0,
    )
}
