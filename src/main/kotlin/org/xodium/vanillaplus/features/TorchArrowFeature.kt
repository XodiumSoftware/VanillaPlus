package org.xodium.vanillaplus.features

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.interfaces.FeatureInterface
import org.xodium.vanillaplus.recipes.TorchArrowRecipe.torchArrowKey

/** Represents a feature handling torch arrow mechanics within the system. */
internal object TorchArrowFeature : FeatureInterface {
    @EventHandler
    fun on(event: ProjectileHitEvent) = torchArrow(event)

    /**
     * Handles the logic for torch arrows when they hit a target.
     * @param event The projectile hit event to process.
     */
    private fun torchArrow(event: ProjectileHitEvent) {
        val arrow = event.entity
        val item = arrow.pickItemStack
        val isTorchArrow = item.editPersistentDataContainer { it.has(torchArrowKey, PersistentDataType.BYTE) }

        if (!isTorchArrow) return

        val hit = arrow.location.block

        if (hit.type == Material.AIR) hit.type = Material.TORCH
    }
}
