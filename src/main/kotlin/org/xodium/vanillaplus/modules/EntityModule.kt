@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Barrel
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.pdcs.AnimalsPDC.searchedFood
import kotlin.random.Random

/** Represents a module handling entity mechanics within the system. */
internal class EntityModule : ModuleInterface<EntityModule.Config> {
    override val config: Config = Config()

    init {
        if (enabled()) {
            instance.server.worlds.forEach { world ->
                world.livingEntities.filterIsInstance<Animals>().forEach { animal -> startSearchTask(animal) }
            }
        }
    }

    @EventHandler
    fun on(event: EntityChangeBlockEvent) {
        if (!enabled()) return
        if (shouldCancelGrief(event.entity)) event.isCancelled = true
    }

    @EventHandler
    fun on(event: EntityExplodeEvent) {
        if (!enabled()) return
        if (shouldCancelGrief(event.entity)) event.blockList().clear()
    }

    @EventHandler
    fun on(event: EntityDeathEvent) {
        if (!enabled() || event.entity.killer == null) return
        if (Random.nextDouble() <= config.entityEggDropChance) {
            event.drops.add(ItemStack.of(Material.matchMaterial("${event.entity.type.name}_SPAWN_EGG") ?: return))
        }
    }

    @EventHandler
    fun on(event: CreatureSpawnEvent) {
        if (!enabled()) return

        val animal = event.entity

        if (animal is Animals) startSearchTask(animal)
    }

    private fun startSearchTask(animal: Animals) {
        if (animal.searchedFood()) return

        animal.searchedFood(true)

        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable {
                if (!animal.isValid || animal.isDead || animal.isLoveMode) return@Runnable

                val foods = getFoodFor(animal) ?: return@Runnable
                val barrel =
                    findBarrelWithFood(
                        animal.location.toVector(),
                        animal.world.name,
                        config.animalDetectFeederRadius,
                        foods,
                    )
                        ?: return@Runnable
                val pathfinder = animal.pathfinder

                pathfinder.moveTo(barrel.location.add(0.5, 0.0, 0.5))

                if (animal.location.distanceSquared(barrel.location) <= 2.25) {
                    consumeOneFood(barrel, foods)
                    animal.loveModeTicks = 600
                    animal.world.spawnParticle(Particle.HEART, animal.location.add(0.0, 1.0, 0.0), 5)
                }
            },
            0L,
            100L,
        )
    }

    /**
     * Removes one valid food item from the barrel inventory.
     * @param barrel The barrel containing the food.
     * @param foods The valid food materials.
     */
    private fun consumeOneFood(
        barrel: Barrel,
        foods: Set<Material>,
    ) {
        barrel.inventory.contents.indices
            .firstOrNull { barrel.inventory.getItem(it)?.type in foods }
            ?.let { index ->
                val item = barrel.inventory.getItem(index)

                if (item?.amount == 1) barrel.inventory.clear(index) else item?.amount = item.amount - 1

                barrel.update()
            }
    }

    /**
     * Finds a nearby barrel that contains any of the provided food items.
     *
     * @param origin The animal’s position.
     * @param worldName The world name.
     * @param radius The search radius.
     * @param foods The valid food items.
     * @return A barrel containing food, or null if none found.
     */
    private fun findBarrelWithFood(
        origin: Vector,
        worldName: String,
        radius: Int,
        foods: Set<Material>,
    ): Barrel? {
        val world = instance.server.getWorld(worldName) ?: return null

        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    val block = world.getBlockAt(origin.blockX + x, origin.blockY + y, origin.blockZ + z)
                    val state = block.state

                    if (state is Barrel && state.inventory.contents.any { it != null && it.type in foods }) return state
                }
            }
        }
        return null
    }

    /**
     * Returns the valid food materials for the specified animal.
     *
     * @param animal The target animal.
     * @return The set of valid food materials, or null if not applicable.
     */
    private fun getFoodFor(animal: Animals): Set<Material>? =
        when (animal.type) {
            EntityType.COW, EntityType.SHEEP -> setOf(Material.WHEAT)

            EntityType.PIG -> setOf(Material.CARROT, Material.BEETROOT, Material.POTATO)

            EntityType.CHICKEN ->
                setOf(
                    Material.WHEAT_SEEDS,
                    Material.BEETROOT_SEEDS,
                    Material.MELON_SEEDS,
                    Material.PUMPKIN_SEEDS,
                )

            EntityType.RABBIT -> setOf(Material.DANDELION, Material.CARROT, Material.BEETROOT)

            else -> null
        }

    /**
     * Determines whether an entity's griefing behaviour should be cancelled based on configuration settings.
     * @param entity The entity whose griefing behaviour is being evaluated.
     * @return `true` if the entity's griefing behaviour should be cancelled; `false` otherwise.
     */
    private fun shouldCancelGrief(entity: Entity): Boolean =
        when (entity) {
            is WitherSkull -> config.disableWitherGrief
            is Fireball -> config.disableGhastGrief
            is Blaze -> config.disableBlazeGrief
            is Creeper -> config.disableCreeperGrief
            is EnderDragon -> config.disableEnderDragonGrief
            is Enderman -> config.disableEndermanGrief
            is Wither -> config.disableWitherGrief
            else -> false
        }

    data class Config(
        var disableBlazeGrief: Boolean = true,
        var disableCreeperGrief: Boolean = true,
        var disableEnderDragonGrief: Boolean = true,
        var disableEndermanGrief: Boolean = true,
        var disableGhastGrief: Boolean = true,
        var disableWitherGrief: Boolean = true,
        var entityEggDropChance: Double = 0.1,
        var animalDetectFeederRadius: Int = 8,
    ) : ModuleInterface.Config
}
