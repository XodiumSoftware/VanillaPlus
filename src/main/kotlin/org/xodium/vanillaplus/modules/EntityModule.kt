@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
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
                world.livingEntities.filterIsInstance<Animals>().forEach { it.searchFood() }
            }
        }
    }

    @EventHandler
    fun on(event: EntityChangeBlockEvent) {
        if (!enabled()) return
        if (event.entity.cancelGrief()) event.isCancelled = true
    }

    @EventHandler
    fun on(event: EntityExplodeEvent) {
        if (!enabled()) return
        if (event.entity.cancelGrief()) event.blockList().clear()
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

        (event.entity as Animals).searchFood()
    }

    private fun Animals.searchFood() {
        if (searchedFood()) return

        searchedFood(true)

        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable {
                if (!isValid || isDead || isLoveMode) return@Runnable

                val foods = getFood() ?: return@Runnable
                val item =
                    findNearbyFoodItem(location, config.animalDetectFoodRadius.toDouble(), foods) ?: return@Runnable

                pathfinder.moveTo(item.location)

                if (location.distanceSquared(item.location) <= 2.25) {
                    item.pickupDelay = 0
                    item.velocity = Vector(0.0, 0.1, 0.0)
                    instance.server.scheduler.runTaskLater(
                        instance,
                        Runnable {
                            if (item.isValid) {
                                item.remove()
                                loveModeTicks = 600
                                world.spawnParticle(Particle.HEART, location.add(0.0, 1.0, 0.0), 5)
                            }
                        },
                        5L,
                    )
                }
            },
            0L,
            100L,
        )
    }

    /**
     * Finds a nearby food item on the ground that matches the animal's preferred-food.
     * @param origin The animal's position.
     * @param radius The search radius.
     * @param foods The valid food materials.
     * @return An item entity containing food, or null if none found.
     */
    private fun findNearbyFoodItem(
        origin: Location,
        radius: Double,
        foods: Set<Material>,
    ): Item? =
        origin.world
            .getNearbyEntities(origin, radius, radius, radius)
            .filterIsInstance<Item>()
            .firstOrNull { item -> item.itemStack.type in foods && item.pickupDelay <= 0 }

    /**
     * Returns the valid food materials for the specified animal.
     * @return The set of valid food materials, or null if not applicable.
     */
    private fun Animals.getFood(): Set<Material>? =
        when (type) {
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
     * @return `true` if the entity's griefing behaviour should be cancelled; `false` otherwise.
     */
    private fun Entity.cancelGrief(): Boolean =
        when (this) {
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
        var animalDetectFoodRadius: Int = 8,
    ) : ModuleInterface.Config
}
