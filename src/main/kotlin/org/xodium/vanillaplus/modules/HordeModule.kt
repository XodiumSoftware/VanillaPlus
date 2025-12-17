package org.xodium.vanillaplus.modules

import kotlinx.serialization.Serializable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Creeper
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.entity.Zombie
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ArmyData
import org.xodium.vanillaplus.data.MonsterData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Represents a module handling horde mechanics within the system. */
internal object HordeModule : ModuleInterface {
    init {
        instance.server.worlds.forEach { world ->
            if (isHordeTime(world)) {
                world.players.forEach { player ->
                    if (Random.nextDouble() < config.hordeModule.armySpawnChancePerPlayer) {
                        val spawnDistance =
                            Random.nextDouble(
                                config.hordeModule.minSpawnDistance,
                                config.hordeModule.maxSpawnDistance,
                            )
                        val angle = Random.nextDouble(0.0, 2 * Math.PI)
                        val spawnLocation =
                            player.location.clone().add(
                                spawnDistance * cos(angle),
                                0.0,
                                spawnDistance * sin(angle),
                            )
                        spawnArmyOfDarkness(world, spawnLocation)
                    }
                }
            }
        }
    }

    /**
     * Spawns an army of darkness at the specified location in the given world.
     * @param world The world where the army will be spawned.
     * @param center The central location for spawning the army.
     */
    private fun spawnArmyOfDarkness(
        world: World,
        center: Location,
    ) {
        val armyConfig = config.hordeModule.armies.random()

        repeat(armyConfig.armySize) { index ->
            instance.server.scheduler.runTaskLater(
                instance,
                Runnable {
                    val monsterConfig = armyConfig.monsters.random()
                    val spawnLocation =
                        center.clone().add(
                            Random.nextDouble(-armyConfig.spawnRadius, armyConfig.spawnRadius),
                            0.0,
                            Random.nextDouble(-armyConfig.spawnRadius, armyConfig.spawnRadius),
                        )

                    spawnLocation.y = world.getHighestBlockYAt(spawnLocation).toDouble()
                    spawnMonster(spawnLocation, monsterConfig, world)
                },
                (index * armyConfig.spawnDelay).toLong(),
            )
        }
    }

    /**
     * Spawns a single monster at the specified location with given configuration.
     * @param location The location where the monster will be spawned.
     * @param monsterConfig The configuration data for the monster.
     * @param world The world where the monster will be spawned.
     */
    private fun spawnMonster(
        location: Location,
        monsterConfig: MonsterData,
        world: World,
    ) {
        val entity = world.spawnEntity(location, monsterConfig.entityType) as? Monster ?: return

        monsterConfig.attributes.forEach { (attribute, range) ->
            entity.getAttribute(attribute)?.baseValue = Random.nextDouble(range.first, range.second)
        }
        entity.health = entity.getAttribute(Attribute.MAX_HEALTH)?.baseValue ?: 20.0
        entity.target = world.getNearbyPlayers(location, config.hordeModule.maxTargetDistance).randomOrNull()

        if (entity is Creeper && Random.nextDouble() < config.hordeModule.chargedCreeperChance) {
            entity.isPowered = true
        }

        if (entity is Zombie) equipZombie(entity)

        val chosenName = monsterConfig.displayNames.randomOrNull()

        if (chosenName != null) {
            entity.customName(chosenName.mm())
            entity.isCustomNameVisible = true
        }
    }

    /**
     * Equips a zombie with armour and weapons based on its health.
     * @param zombie The zombie entity to equip.
     */
    private fun equipZombie(zombie: Zombie) {
        val health = zombie.getAttribute(Attribute.MAX_HEALTH)?.baseValue ?: 20.0
        val maxHealth = 40.0
        val healthRatio = (health - 10.0) / (maxHealth - 10.0)
        val armor =
            when {
                healthRatio < 0.33 -> "LEATHER"
                healthRatio < 0.66 -> "CHAINMAIL"
                else -> "IRON"
            }
        val weapon =
            when (Random.nextInt(3)) {
                0 -> Material.IRON_SWORD
                1 -> Material.IRON_AXE
                else -> Material.IRON_SPEAR
            }

        zombie.equipment.apply {
            helmet = ItemStack.of(Material.valueOf("${armor}_HELMET"))
            chestplate = ItemStack.of(Material.valueOf("${armor}_CHESTPLATE"))
            leggings = ItemStack.of(Material.valueOf("${armor}_LEGGINGS"))
            boots = ItemStack.of(Material.valueOf("${armor}_BOOTS"))

            setItemInMainHand(ItemStack.of(weapon))

            helmetDropChance = 0.05f
            chestplateDropChance = 0.05f
            leggingsDropChance = 0.05f
            bootsDropChance = 0.05f
            itemInMainHandDropChance = 0.05f
        }
    }

    /**
     * Determines if the current world time corresponds to a horde night.
     * @param world The world to check.
     * @return True if it's a horde night, false otherwise.
     */
    private fun isHordeTime(world: World): Boolean {
        val day = world.fullTime / 24000
        val seed = world.seed xor day
        val rng = Random(seed)

        return rng.nextDouble() < config.hordeModule.hordeChance
    }

    @Serializable
    data class Config(
        val enabled: Boolean = true,
        val hordeChance: Double = 1.0 / 7.0,
        val maxTargetDistance: Double = 32.0,
        val chargedCreeperChance: Double = 0.1,
        val armySpawnChancePerPlayer: Double = 0.5,
        val minSpawnDistance: Double = 40.0,
        val maxSpawnDistance: Double = 80.0,
        val armies: List<ArmyData> =
            listOf(
                ArmyData(
                    name = "Undead Legion",
                    armySize = 50,
                    spawnRadius = 20.0,
                    spawnDelay = 10,
                    monsters =
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
                ),
            ),
    )
}
