package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Zombie
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import kotlin.random.Random

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

            config.hordeModule.attributeRanges.forEach { (attribute, range) ->
                zombie.getAttribute(attribute)?.baseValue = range(range)
            }

            zombie.health = zombie.getAttribute(Attribute.MAX_HEALTH)?.baseValue ?: 20.0
            zombie.target = world.getNearbyPlayers(location, config.hordeModule.maxTargetDistance).randomOrNull()
        }
    }

    /**
     * Generates a random value within the specified range.
     * @param pair The range defined by a pair of doubles.
     * @return A random double within the specified range.
     */
    private fun range(pair: Pair<Double, Double>): Double = Random.nextDouble(pair.first, pair.second)

    @Serializable
    data class Config(
        val enabled: Boolean = true,
        val spawnModifier: Int = 199,
        val maxTargetDistance: Double = 32.0,
        val attributeRanges: Map<Attribute, Pair<Double, Double>> =
            mapOf(
                Attribute.ATTACK_DAMAGE to Pair(2.0, 8.0),
                Attribute.ATTACK_SPEED to Pair(3.0, 6.0),
                Attribute.EXPLOSION_KNOCKBACK_RESISTANCE to Pair(0.0, 0.5),
                Attribute.JUMP_STRENGTH to Pair(0.4, 1.2),
                Attribute.KNOCKBACK_RESISTANCE to Pair(0.0, 0.6),
                Attribute.MAX_HEALTH to Pair(10.0, 40.0),
                Attribute.MOVEMENT_EFFICIENCY to Pair(0.0, 1.0),
                Attribute.MOVEMENT_SPEED to Pair(0.18, 0.35),
                Attribute.OXYGEN_BONUS to Pair(0.0, 20.0),
                Attribute.SCALE to Pair(0.6, 1.6),
                Attribute.SPAWN_REINFORCEMENTS to Pair(0.0, 0.3),
                Attribute.STEP_HEIGHT to Pair(0.6, 1.2),
                Attribute.WATER_MOVEMENT_EFFICIENCY to Pair(0.0, 1.0),
            ),
    )
}
