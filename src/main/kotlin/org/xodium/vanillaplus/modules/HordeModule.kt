package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.entity.EntityType
import org.bukkit.entity.Zombie
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface

/** Represents a module handling horde mechanics within the system. */
internal object HordeModule : ModuleInterface {
    @EventHandler
    fun on(event: CreatureSpawnEvent) = handleCreatureSpawn(event)

    /**
     * Handles the creature spawn event to create a horde effect.
     * @param event The creature spawn event to handle.
     */
    private fun handleCreatureSpawn(event: CreatureSpawnEvent) {
        if (event.entity.world.isDayTime ||
            event.spawnReason != CreatureSpawnEvent.SpawnReason.NATURAL ||
            event.entityType != EntityType.ZOMBIE
        ) {
            return
        }

        val location = event.location
        val world = location.world

        repeat(config.hordeModule.spawnModifier) {
            val zombie = world.spawnEntity(location, event.entityType) as Zombie

            zombie.target =
                world
                    .getNearbyPlayers(location, config.hordeModule.maxTargetDistance)
                    .randomOrNull()
        }
    }

    @Serializable
    data class Config(
        val enabled: Boolean = true,
        val spawnModifier: Int = 199,
        val maxTargetDistance: Double = 32.0,
    )
}
