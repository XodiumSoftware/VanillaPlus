@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import org.bukkit.EntityEffect
import org.bukkit.Location
import org.bukkit.Material
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
import org.xodium.vanillaplus.pdcs.AnimalsPDC.searchingFood
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
        if (searchingFood) return

        searchingFood = true

        instance.server.scheduler.runTaskTimer(
            instance,
            Runnable {
                if (!isValid || isDead || isLoveMode) return@Runnable

                val item =
                    findNearbyItem(
                        location,
                        config.animalDetectFoodRadius.toDouble(),
                        Material.entries.filter { isBreedItem(it) }.toSet(),
                    ) ?: return@Runnable

                pathfinder.moveTo(item.location)

                if (location.distanceSquared(item.location) <= 2.25) {
                    item.pickupDelay = 0
                    item.velocity = Vector(0.0, 0.1, 0.0)

                    playPickupItemAnimation(item, 1)
                    when (this) {
                        // TODO: Add more animals that have eating effects
                        is Sheep -> playEffect(EntityEffect.SHEEP_EAT_GRASS)
                    }

                    instance.server.scheduler.runTaskLater(
                        instance,
                        Runnable {
                            loveModeTicks = 600
                            playEffect(EntityEffect.LOVE_HEARTS)
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
     * Finds a nearby item of specified types within a given radius from the origin location.
     * @param origin The origin location to search from.
     * @param radius The radius within which to search for items.
     * @param items The set of item types to look for.
     * @return The first found item of the specified types within the radius, or `null` if none are found.
     */
    private fun findNearbyItem(
        origin: Location,
        radius: Double,
        items: Set<Material>,
    ): Item? =
        origin.world
            .getNearbyEntities(origin, radius, radius, radius)
            .filterIsInstance<Item>()
            .firstOrNull { it.itemStack.type in items && it.pickupDelay <= 0 }

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
