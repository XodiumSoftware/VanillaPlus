package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Creeper
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.xodium.vanillaplus.data.MonsterData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
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
            !isHordeNight(event.entity.world)
        ) {
            return
        }

        val monsterConfig = config.hordeModule.monsters.firstOrNull { it.entityType == event.entityType } ?: return
        val location = event.location
        val world = location.world

        repeat(monsterConfig.spawnModifier) {
            val entity = world.spawnEntity(location, event.entityType) as? Monster ?: return

            monsterConfig.attributes.forEach { (attribute, range) ->
                entity.getAttribute(attribute)?.baseValue = Random.nextDouble(range.first, range.second)
            }
            entity.health = entity.getAttribute(Attribute.MAX_HEALTH)?.baseValue ?: 20.0
            entity.target = world.getNearbyPlayers(location, config.hordeModule.maxTargetDistance).randomOrNull()

            if (entity is Creeper && Random.nextDouble() < config.hordeModule.chargedCreeperChance) {
                entity.isPowered = true
            }

            val chosenName = monsterConfig.displayNames.randomOrNull()

            if (chosenName != null) {
                entity.customName(chosenName.mm())
                entity.isCustomNameVisible = true
            }
        }
    }

    /**
     * Determines if the current world time corresponds to a horde night.
     * @param world The world to check.
     * @return True if it's a horde night, false otherwise.
     */
    private fun isHordeNight(world: World): Boolean {
        val day = world.fullTime / 24000
        val seed = world.seed xor day
        val rng = Random(seed)

        return rng.nextDouble() < config.hordeModule.nightChance
    }

    @Serializable
    data class Config(
        val enabled: Boolean = true,
        val nightChance: Double = 1.0 / 7.0,
        val maxTargetDistance: Double = 32.0,
        val chargedCreeperChance: Double = 0.1,
        val monsters: List<MonsterData> =
            listOf(
                MonsterData(
                    entityType = EntityType.ZOMBIE,
                    spawnModifier = 199,
                    attributes =
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
                    displayNames = listOf("Gorath", "Thugor", "Morg", "Grimnar"),
                ),
                MonsterData(
                    entityType = EntityType.SPIDER,
                    spawnModifier = 199,
                    attributes =
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
                    displayNames = listOf("Silkfang", "Nightweaver", "Broodmother"),
                ),
                MonsterData(
                    entityType = EntityType.CREEPER,
                    spawnModifier = 199,
                    attributes =
                        mapOf(
                            Attribute.EXPLOSION_KNOCKBACK_RESISTANCE to Pair(0.0, 0.5),
                            Attribute.KNOCKBACK_RESISTANCE to Pair(0.0, 0.4),
                            Attribute.MAX_HEALTH to Pair(15.0, 35.0),
                            Attribute.MOVEMENT_SPEED to Pair(0.2, 0.35),
                            Attribute.SCALE to Pair(0.8, 1.5),
                            Attribute.STEP_HEIGHT to Pair(0.6, 1.0),
                        ),
                    displayNames = listOf("Singe", "Boombrood", "Skar"),
                ),
            ),
    )
}
