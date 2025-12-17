package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Creeper
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.xodium.vanillaplus.data.MonsterData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.recipes.CursedClockRecipe
import org.xodium.vanillaplus.recipes.CursedClockRecipe.isCursedClock
import org.xodium.vanillaplus.utils.ExtUtils.mm
import kotlin.random.Random

/** Represents a module handling horde mechanics within the system. */
internal object HordeModule : ModuleInterface {
    @EventHandler
    fun on(event: CreatureSpawnEvent) = handleCreatureSpawn(event)

    @EventHandler
    fun on(event: PlayerItemHeldEvent) = handlePlayerItemHeld(event)

    init {
        CursedClockRecipe.register()
    }

    /**
     * Handles the creature spawn event to create a horde effect.
     * @param event The creature spawn event to handle.
     */
    private fun handleCreatureSpawn(event: CreatureSpawnEvent) {
        if (event.entity.world.isDayTime ||
            event.spawnReason != CreatureSpawnEvent.SpawnReason.NATURAL ||
            !isHordeNight(event.entity.world.fullTime)
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
     * Handles the player item held event to display horde timer if holding a cursed clock.
     * @param event The player item held event to handle.
     */
    private fun handlePlayerItemHeld(event: PlayerItemHeldEvent) {
        val player = event.player
        val newItem = player.inventory.getItem(event.newSlot) ?: return

        if (isCursedClock(newItem)) displayHordeTimer(player)
    }

    /**
     * Displays the time until the next horde in the player's action bar.
     * @param player The player to display the timer to.
     */
    private fun displayHordeTimer(player: Player) {
        val world = player.world
        val currentTime = world.fullTime
        val currentDay = (currentTime / 24000) + 1
        val nextHordeDay = ((currentDay / config.hordeModule.nightInterval) + 1) * config.hordeModule.nightInterval
        val daysUntilHorde = nextHordeDay - currentDay
        val message =
            if (daysUntilHorde == 0L && !world.isDayTime) {
                "<red>HORDE NIGHT!"
            } else {
                "<yellow>Next horde in: $daysUntilHorde night${if (daysUntilHorde != 1L) "s" else ""}"
            }

        player.sendActionBar(message.mm())
    }

    /**
     * Determines if the current world time corresponds to a horde night.
     * @param worldTime The current world time.
     * @return True if it's a horde night, false otherwise.
     */
    private fun isHordeNight(worldTime: Long): Boolean =
        (worldTime / 24000) + 1 % config.hordeModule.nightInterval == 0L

    @Serializable
    data class Config(
        val enabled: Boolean = true,
        val nightInterval: Long = 7,
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
