@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

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
internal class SitModule : ModuleInterface<SitModule.Config> {
    override val config: Config = Config()

    private val sittingPlayers = mutableMapOf<UUID, ArmorStand>()
    private val blockCenterOffset = Vector(0.5, 0.5, 0.5)
    private val playerStandUpOffset = Vector(0.0, 0.5, 0.0)

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerInteractEvent) {
        if (!enabled()) return

        val player = event.player

        if (event.action != Action.RIGHT_CLICK_BLOCK || player.isSneaking || player.isInsideVehicle) return
        if (player.inventory.itemInMainHand.type != Material.AIR) return

        val block = event.clickedBlock ?: return
        val blockData = block.blockData
        val isSitTarget =
            when {
                config.useStairs && blockData is Stairs && blockData.half == Bisected.Half.BOTTOM -> true
                config.useSlabs && blockData is Slab && blockData.type == Slab.Type.BOTTOM -> true
                else -> false
            }

        if (!isSitTarget) return

        val blockAbove = block.getRelative(BlockFace.UP)

        if (blockAbove.type.isCollidable) return

        event.isCancelled = true
        sit(player, block.location.add(blockCenterOffset))
    }

    @EventHandler
    fun on(event: EntityDismountEvent) {
        if (!enabled()) return
        if (event.entity !is Player) return

        val player = event.entity as Player

        sittingPlayers.remove(player.uniqueId)?.let { armorStand ->
            val safeLocation = armorStand.location.clone().add(playerStandUpOffset)
            safeLocation.yaw = player.location.yaw
            safeLocation.pitch = player.location.pitch
            player.teleport(safeLocation)
            armorStand.remove()
        }
    }

    @EventHandler
    fun on(event: PlayerQuitEvent) {
        if (!enabled()) return

        sittingPlayers.remove(event.player.uniqueId)?.remove()
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: EntityDamageEvent) {
        if (!enabled()) return

        val player = event.entity as? Player ?: return

        sittingPlayers[player.uniqueId]?.let { armorStand ->
            armorStand.removePassenger(player)
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

    data class Config(
        var useStairs: Boolean = true,
        var useSlabs: Boolean = true,
    ) : ModuleInterface.Config
}
