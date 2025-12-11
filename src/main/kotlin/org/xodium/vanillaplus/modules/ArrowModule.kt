package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import org.bukkit.entity.Arrow
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.recipes.TorchArrowRecipe

/** Represents a module handling custom arrow mechanics within the system. */
internal object ArrowModule : ModuleInterface {
    @EventHandler
    fun on(event: ProjectileLaunchEvent) = handleProjectileLaunch(event)

    @EventHandler
    fun on(event: ProjectileHitEvent) = handleProjectileHit(event)

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: EntityDamageByEntityEvent) = handleEntityDamage(event)

    /**
     * Applies visual tipped-arrow particles to torch arrows.
     * @param event The projectile launch event.
     */
    private fun handleProjectileLaunch(event: ProjectileLaunchEvent) {
        val arrow = event.entity as? Arrow ?: return
        val torchTypeId = arrow.torchArrowType ?: return
        val torchType = TorchArrowRecipe.getTorchArrowTypeById(torchTypeId) ?: return

        arrow.color = torchType.arrowColor
    }

    /**
     * Handles the logic for torch arrows when they hit a target.
     * @param event The projectile hit event to process.
     */
    private fun handleProjectileHit(event: ProjectileHitEvent) {
        val arrow = event.entity as? Arrow ?: return
        val torchTypeId = arrow.torchArrowType ?: return
        val torchType = TorchArrowRecipe.getTorchArrowTypeById(torchTypeId) ?: return
        val hitBlock = event.hitBlock
        val hitFace = event.hitBlockFace

        if (hitBlock == null || hitFace == null) {
            dropArrow(arrow, arrow.location.block.location)
            return
        }

        val target = hitBlock.getRelative(hitFace)

        if (hitBlock.type == Material.VINE) {
            hitBlock.breakNaturally()
            target.type = torchType.torchMaterial
            arrow.remove()
            return
        }

        when (hitFace) {
            BlockFace.UP -> {
                if (target.type == Material.AIR) {
                    target.type = torchType.torchMaterial
                } else {
                    dropArrow(arrow, target.location)
                }
            }

            BlockFace.DOWN -> {
                dropArrow(arrow, target.location)
            }

            else -> {
                if (target.type != Material.AIR) {
                    dropArrow(arrow, target.location)
                    return
                }

                target.type = torchType.wallTorchMaterial

                val data = target.blockData as? Directional

                data?.facing = hitFace
                data?.let { target.blockData = it }
            }
        }

        arrow.remove()
    }

    /**
     * Handles the logic for torch arrows when they attempt to deal damage.
     * @param event The entity damage event to process.
     */
    private fun handleEntityDamage(event: EntityDamageByEntityEvent) {
        val arrow = event.damager as? Arrow ?: return

        if (arrow.torchArrowType == null) return

        arrow.remove()
        event.isCancelled = true
    }

    /**
     * Drops a torch arrow item at the specified location.
     * @param location The location where the torch arrow should be dropped.
     * @param arrow The arrow to be dropped.
     */
    private fun dropArrow(
        arrow: Arrow,
        location: Location,
    ) {
        location.world.dropItemNaturally(location, arrow.itemStack)
    }

    /**
     * Checks whether this arrow is a torch arrow based on its ItemStack metadata.
     * @return True if the arrow is a torch arrow, false otherwise.
     */
    private val Arrow.torchArrowType: String?
        get() = itemStack.persistentDataContainer.get(TorchArrowRecipe.torchArrowKey, PersistentDataType.STRING)

    @Serializable
    data class Config(
        var enabled: Boolean = true,
    )
}
