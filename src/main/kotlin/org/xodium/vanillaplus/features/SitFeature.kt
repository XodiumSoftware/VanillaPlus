@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.features

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
import org.xodium.vanillaplus.interfaces.FeatureInterface
import java.util.*

/** Represents a feature handling sit mechanics within the system. */
internal object SitFeature : FeatureInterface {
    private val sittingPlayers = mutableMapOf<UUID, ArmorStand>()
    private val blockCenterOffset = Vector(0.5, 0.5, 0.5)
    private val playerStandUpOffset = Vector(0.0, 0.5, 0.0)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) = handleInteract(event)

    @EventHandler
    fun on(event: EntityDismountEvent) = handleDismount(event)

    @EventHandler
    fun on(event: PlayerQuitEvent) = handleQuit(event)

    @EventHandler(ignoreCancelled = true)
    fun on(event: EntityDamageEvent) = handleDamage(event)

    /**
     * Handles player interaction to initiate sitting.
     * @param event The [PlayerInteractEvent] triggered by the player.
     */
    private fun handleInteract(event: PlayerInteractEvent) {
        val player = event.player

        if (event.action != Action.RIGHT_CLICK_BLOCK || player.isSneaking || player.isInsideVehicle) return
        if (player.inventory.itemInMainHand.type != Material.AIR) return

        val block = event.clickedBlock ?: return
        val blockData = block.blockData

        val isSitTarget =
            when {
                config.sitFeature.useStairs && blockData is Stairs && blockData.half == Bisected.Half.BOTTOM -> true
                config.sitFeature.useSlabs && blockData is Slab && blockData.type == Slab.Type.BOTTOM -> true
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
    private fun handleDismount(event: EntityDismountEvent) {
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
     * Handles cleanup when a player quits.
     * @param event The [PlayerQuitEvent] triggered when the player leaves the server.
     */
    private fun handleQuit(event: PlayerQuitEvent) = sittingPlayers.remove(event.player.uniqueId)?.remove()

    /**
     * Handles player damage while sitting.
     * @param event The [EntityDamageEvent] triggered when the player takes damage.
     */
    private fun handleDamage(event: EntityDamageEvent) {
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
}
