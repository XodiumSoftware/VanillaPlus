@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.Vector
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.util.*

/** Represents a module handling sit mechanics within the system. */
internal object SitModule : ModuleInterface {
    private val sittingPlayers = mutableMapOf<UUID, ArmorStand>()
    private val blockCenterOffset = Vector(0.5, 0.5, 0.5)
    private val playerStandUpOffset = Vector(0.0, 0.5, 0.0)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) = playerInteract(event)

    @EventHandler
    fun on(event: EntityDismountEvent) = entityDismount(event)

    @EventHandler
    fun on(event: PlayerQuitEvent) = playerQuit(event)

    @EventHandler(ignoreCancelled = true)
    fun on(event: EntityDamageEvent) = entityDamage(event)

    /**
     * Handles player interaction to initiate sitting.
     * @param event The [PlayerInteractEvent] triggered by the player.
     */
    private fun playerInteract(event: PlayerInteractEvent) {
        val player = event.player

        if (event.action != Action.RIGHT_CLICK_BLOCK || player.isSneaking || player.isInsideVehicle) return
        if (player.inventory.itemInMainHand.type != Material.AIR) return

        val block = event.clickedBlock ?: return
        val blockData = block.blockData

        val isSitTarget =
            when {
                config.sitModule.useStairs && blockData is Stairs && blockData.half == Bisected.Half.BOTTOM -> true
                config.sitModule.useSlabs && blockData is Slab && blockData.type == Slab.Type.BOTTOM -> true
                else -> false
            }

        if (!isSitTarget) return

        if (block.getRelative(BlockFace.UP).type.isCollidable) return

        event.isCancelled = true
        sit(player, block.location.add(blockCenterOffset))
    }

    /**
     * Handles dismounting from the sitting ArmorStand.
     * @param event The [EntityDismountEvent] triggered when the player dismounts.
     */
    private fun entityDismount(event: EntityDismountEvent) {
        val player = event.entity as? Player ?: return

        sittingPlayers.remove(player.uniqueId)?.let { armorStand ->
            val safe = armorStand.location.clone().add(playerStandUpOffset)
            safe.yaw = player.location.yaw
            safe.pitch = player.location.pitch
            player.teleport(safe)
            armorStand.remove()
        }
    }

    /**
     * Handles clean-up when a player quits.
     * @param event The [PlayerQuitEvent] triggered when the player leaves the server.
     */
    private fun playerQuit(event: PlayerQuitEvent) {
        sittingPlayers.remove(event.player.uniqueId)?.remove()
    }

    /**
     * Handles player damage while sitting.
     * @param event The [EntityDamageEvent] triggered when the player takes damage.
     */
    private fun entityDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return

        sittingPlayers[player.uniqueId]?.let { stand ->
            stand.removePassenger(player)
            sittingPlayers.remove(player.uniqueId)
        }
    }

    /**
     * Spawns an invisible, marker ArmorStand at the given location and makes the player sit on it.
     * @param player The [Player] who will be made to sit.
     * @param location The [Location] where the player should sit.
     */
    private fun sit(
        player: Player,
        location: Location,
    ) {
        val world = location.world ?: return
        val armorStand =
            world.spawn(location, ArmorStand::class.java) {
                it.isVisible = false
                it.setGravity(false)
                it.isSmall = true
                it.isMarker = true
            }

        armorStand.addPassenger(player)
        sittingPlayers[player.uniqueId] = armorStand
    }

    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var useStairs: Boolean = true,
        var useSlabs: Boolean = true,
    )
}
