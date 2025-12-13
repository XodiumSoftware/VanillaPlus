package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Creeper
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.serializers.AttributeSerializer
import kotlin.random.Random

typealias AttributeRangeMap = Map<
    @Serializable(with = AttributeSerializer::class)
    Attribute,
    Pair<Double, Double>,
>

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
            event.spawnReason != CreatureSpawnEvent.SpawnReason.NATURAL
        ) {
            return
        }

        val attributeMap =
            when (event.entityType) {
                EntityType.ZOMBIE -> config.hordeModule.zombieAttributes
                EntityType.SPIDER -> config.hordeModule.spiderAttributes
                EntityType.CREEPER -> config.hordeModule.creeperAttributes
                else -> return
            }

        val location = event.location
        val world = location.world

        repeat(config.hordeModule.spawnModifier) {
            val entity = world.spawnEntity(location, event.entityType) as? Monster ?: return

            attributeMap.forEach { (attribute, range) -> entity.getAttribute(attribute)?.baseValue = range(range) }
            entity.health = entity.getAttribute(Attribute.MAX_HEALTH)?.baseValue ?: 20.0
            entity.target = world.getNearbyPlayers(location, config.hordeModule.maxTargetDistance).randomOrNull()

            if (entity is Creeper && Random.nextDouble() < config.hordeModule.chargedCreeperChance) {
                entity.isPowered = true
            }
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
        val zombieAttributes: AttributeRangeMap =
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
        val spiderAttributes: AttributeRangeMap =
            mapOf(
                Attribute.ATTACK_DAMAGE to Pair(2.0, 6.0),
                Attribute.ATTACK_SPEED to Pair(3.0, 5.0),
                Attribute.JUMP_STRENGTH to Pair(0.5, 1.5),
                Attribute.KNOCKBACK_RESISTANCE to Pair(0.0, 0.4),
                Attribute.MAX_HEALTH to Pair(12.0, 30.0),
                Attribute.MOVEMENT_SPEED to Pair(0.25, 0.4),
                Attribute.SCALE to Pair(0.7, 1.4),
                Attribute.STEP_HEIGHT to Pair(0.6, 1.0),
            ),
        val creeperAttributes: AttributeRangeMap =
            mapOf(
                Attribute.EXPLOSION_KNOCKBACK_RESISTANCE to Pair(0.0, 0.5),
                Attribute.KNOCKBACK_RESISTANCE to Pair(0.0, 0.4),
                Attribute.MAX_HEALTH to Pair(15.0, 35.0),
                Attribute.MOVEMENT_SPEED to Pair(0.2, 0.35),
                Attribute.SCALE to Pair(0.8, 1.5),
                Attribute.STEP_HEIGHT to Pair(0.6, 1.0),
            ),
        val chargedCreeperChance: Double = 0.1,
    )
}
