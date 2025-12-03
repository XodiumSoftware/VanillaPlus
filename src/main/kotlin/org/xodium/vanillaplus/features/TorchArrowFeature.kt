package org.xodium.vanillaplus.features

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import org.bukkit.entity.Arrow
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.interfaces.FeatureInterface
import org.xodium.vanillaplus.recipes.TorchArrowRecipe.torchArrowKey

/** Represents a feature handling torch arrow mechanics within the system. */
internal object TorchArrowFeature : FeatureInterface {
    @EventHandler
    fun on(event: ProjectileLaunchEvent) = handleProjectileLaunch(event)

    @EventHandler
    fun on(event: ProjectileHitEvent) = handleProjectileHit(event)

    /**
     * Handles the logic for torch arrows when they are launched.
     * @param event The projectile launch event to process.
     */
    private fun handleProjectileLaunch(event: ProjectileLaunchEvent) {
        val projectile = event.entity as? Arrow ?: return

        if (projectile.isTorchArrow) return

        val item = projectile.itemStack.clone()

        item.itemMeta?.persistentDataContainer?.set(torchArrowKey, PersistentDataType.BYTE, 1)
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
            dropTorch(arrow.location.block.location)
            return
        }

        val target = hitBlock.getRelative(hitFace)

        if (event.hitEntity != null) {
            dropTorch(target.location)
            return
        }

        if (hitBlock.type == Material.VINE) {
            hitBlock.breakNaturally()
            target.type = Material.TORCH
            return
        }

        when (hitFace) {
            BlockFace.UP -> {
                if (target.type == Material.AIR) {
                    target.type = Material.TORCH
                } else {
                    dropTorch(target.location)
                }
            }

            BlockFace.DOWN -> {
                dropTorch(target.location)
            }

            else -> {
                if (target.type != Material.AIR) {
                    dropTorch(target.location)
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
     * Drops a torch item naturally at the specified location.
     * @param location The location where the torch should be dropped.
     */
    private fun dropTorch(location: Location) = location.world.dropItemNaturally(location, ItemStack.of(Material.TORCH))

    /**
     * Checks whether this arrow is a torch arrow based on its ItemStack metadata.
     * @return True if the arrow is a torch arrow, false otherwise.
     */
    private val Arrow.isTorchArrow: Boolean
        get() = itemStack.persistentDataContainer.has(torchArrowKey, PersistentDataType.BYTE)
}
