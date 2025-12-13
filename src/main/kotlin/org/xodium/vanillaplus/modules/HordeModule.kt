package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.entity.EntityType
import org.bukkit.entity.Zombie
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.lang.Math.random
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
        val entityType = event.entityType

        repeat(config.hordeModule.spawnModifier) {
            val angle = random() * 2 * PI
            val distance = random() * 2.0 + 1.0
            val spawnLocation = location.clone().add(cos(angle) * distance, 0.0, sin(angle) * distance)

            spawnLocation.y =
                location.world
                    .getHighestBlockAt(spawnLocation)
                    .location.y

            val zombie = location.world.spawnEntity(spawnLocation, entityType) as Zombie

            zombie.target =
                event.entity.world.players
                    .minByOrNull { it.location.distance(location) }
        }
    }

    @Serializable
    data class Config(
        val enabled: Boolean = true,
        val spawnModifier: Int = 199,
    )
}
