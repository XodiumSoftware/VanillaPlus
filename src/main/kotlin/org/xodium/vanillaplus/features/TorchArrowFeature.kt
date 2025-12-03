package org.xodium.vanillaplus.features

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
import org.xodium.vanillaplus.interfaces.FeatureInterface
import org.xodium.vanillaplus.recipes.TorchArrowRecipe.torchArrow
import org.xodium.vanillaplus.recipes.TorchArrowRecipe.torchArrowKey

/** Represents a feature handling torch arrow mechanics within the system. */
internal object TorchArrowFeature : FeatureInterface {
    @EventHandler
    fun on(event: ProjectileLaunchEvent) = handleProjectileLaunch(event)

    @EventHandler
    fun on(event: ProjectileHitEvent) = handleProjectileHit(event)

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: EntityDamageByEntityEvent) = handleEntityDamage(event)

    /**
     * Handles the logic for torch arrows when they are launched.
     * @param event The projectile launch event to process.
     */
    private fun handleProjectileLaunch(event: ProjectileLaunchEvent) {
        val projectile = event.entity as? Arrow ?: return

        if (projectile.isTorchArrow) return

        val item = projectile.itemStack

        item.editPersistentDataContainer { it.set(torchArrowKey, PersistentDataType.BYTE, 1) }
        projectile.itemStack = item
    }

    /**
     * Handles the logic for torch arrows when they hit a target.
     * @param event The projectile hit event to process.
     */
    private fun handleProjectileHit(event: ProjectileHitEvent) {
        val arrow = event.entity as? Arrow ?: return

        if (!arrow.isTorchArrow) return

        val hitBlock = event.hitBlock
        val hitFace = event.hitBlockFace

        if (hitBlock == null || hitFace == null) {
            dropArrow(arrow.location.block.location)
            return
        }

        val target = hitBlock.getRelative(hitFace)

        if (event.hitEntity != null) {
            dropArrow(target.location)
            return
        }

        if (hitBlock.type == Material.VINE) {
            hitBlock.breakNaturally()
            target.type = Material.TORCH
            return
        }

        when (hitFace) {
            BlockFace.UP -> {
                if (target.type == Material.AIR) target.type = Material.TORCH else dropArrow(target.location)
            }

            BlockFace.DOWN -> {
                dropArrow(target.location)
            }

            else -> {
                if (target.type != Material.AIR) {
                    dropArrow(target.location)
                    return
                }

                target.type = Material.WALL_TORCH

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
        val damager = event.damager as? Arrow ?: return

        if (!damager.isTorchArrow) return

        event.isCancelled = true
    }

    /**
     * Drops a torch arrow item at the specified location.
     * @param location The location where the torch arrow should be dropped.
     */
    private fun dropArrow(location: Location) = location.world.dropItemNaturally(location, torchArrow)

    /**
     * Checks whether this arrow is a torch arrow based on its ItemStack metadata.
     * @return True if the arrow is a torch arrow, false otherwise.
     */
    private val Arrow.isTorchArrow: Boolean
        get() = itemStack.persistentDataContainer.has(torchArrowKey, PersistentDataType.BYTE)
}
