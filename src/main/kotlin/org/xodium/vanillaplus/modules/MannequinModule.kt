package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.Location
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
import org.bukkit.util.Vector
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.dialogs.MannequinDialog.dialog
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.pdcs.MannequinPDC.owner
import java.util.*
import kotlin.math.atan2
import kotlin.math.sqrt

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
        mannequins.forEach {
            if (!it.isValid || it.isDead) {
                trackingMannequins.remove(it.uniqueId)
                return@forEach
            }

            val nearestPlayer = findNearestPlayerNearby(it)

            if (nearestPlayer != null) lookAt(it, nearestPlayer.location) else resetHeadRotation(it)
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

    private fun lookAt(
        mannequin: Mannequin,
        target: Location,
    ) {
        val direction = target.toVector().subtract(mannequin.location.toVector())

        val yaw = getYaw(direction)
        val pitch = getPitch(direction)

        mannequin.setHeadPose(mannequin.headPose.setYaw(yaw.toDouble()))
        mannequin.setHeadPose(mannequin.headPose.setPitch(pitch.toDouble()))
    }

    private fun getYaw(direction: Vector): Float {
        val x = direction.x
        val z = direction.z

        if (x == 0.0 && z == 0.0) return 0f

        val theta = atan2(-x, z).toFloat()

        return Math.toDegrees(theta.toDouble()).toFloat()
    }

    private fun getPitch(direction: Vector): Float {
        val x = direction.x
        val y = direction.y
        val z = direction.z

        if (x == 0.0 && y == 0.0 && z == 0.0) return 0f

        val horizontalDistance = sqrt((x * x + z * z))
        val theta = atan2(y, horizontalDistance).toFloat()

        return -Math.toDegrees(theta.toDouble()).toFloat()
    }

    private fun resetHeadRotation(mannequin: Mannequin) {
        mannequin.setHeadPose(mannequin.headPose.setYaw(0.0))
    }

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var triggerItem: Material = Material.TOTEM_OF_UNDYING,
        var lookRange: Double = 10.0,
    )
}
