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
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.FeatureInterface
import org.xodium.vanillaplus.recipes.TorchArrowRecipe.torchArrowKey

/** Represents a feature handling torch arrow mechanics within the system. */
internal object TorchArrowFeature : FeatureInterface {
    @EventHandler
    fun on(event: ProjectileLaunchEvent) = handleTorchArrowLaunch(event)

    @EventHandler
    fun on(event: ProjectileHitEvent) = handleTorchArrowHit(event)

    /**
     * Handles the logic for torch arrows when they are launched.
     * @param event The projectile launch event to process.
     */
    private fun handleTorchArrowLaunch(event: ProjectileLaunchEvent) {
        val projectile = event.entity

        if (projectile !is Arrow) return

        val arrowItem = projectile.itemStack
        // TODO: make it an extension function to Arrow.
        val isTorchArrow =
            arrowItem.hasItemMeta() && arrowItem.itemMeta?.persistentDataContainer?.has(
                torchArrowKey,
                PersistentDataType.BYTE,
            ) == true

        // TODO: move away from deprecated stuff.
        if (isTorchArrow) projectile.setMetadata("torch_arrow", FixedMetadataValue(instance, true))
    }

    /**
     * Handles the logic for torch arrows when they hit a target.
     * @param event The projectile hit event to process.
     */
    private fun handleTorchArrowHit(event: ProjectileHitEvent) {
        val arrow = event.entity
        val isTorchArrow = arrow.hasMetadata("torch_arrow")

        if (!isTorchArrow) return

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
}
