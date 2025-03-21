/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.NamespacedKey
import org.bukkit.entity.Painting
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import kotlin.random.Random

class CustomArtModule : ModuleInterface {
    override fun enabled(): Boolean = Config.CustomArtModule.ENABLED

    private var minCMD: Int = 1
    private var maxCMD: Int = 10

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: HangingPlaceEvent) {
        val entity = event.entity
        if (entity !is Painting) return
        try {
            val artRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.PAINTING_VARIANT)

            val artList = artRegistry.stream().toList()
            if (artList.isEmpty()) {
                instance.logger.warning("No painting variants found in registry")
                return
            }

            val randomArt = artList[Random.nextInt(artList.size)]
            entity.art = randomArt

            val dataContainer = entity.persistentDataContainer
            val customPaintingId = Random.nextInt(minCMD, maxCMD + 1)

            dataContainer.set(
                NamespacedKey(instance, "custom-painting-id"),
                PersistentDataType.INTEGER,
                customPaintingId
            )
        } catch (e: Exception) {
            instance.logger.warning("Error setting random painting: ${e.message}")
        }
    }
}