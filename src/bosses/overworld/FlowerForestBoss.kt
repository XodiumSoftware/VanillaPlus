package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Bee
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface
import kotlin.random.Random

/**
 * A pollen spirit that thrives among the flowers.
 */
internal object FlowerForestBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FFB6C1:#FFD700:#FFF0F5>Flora, the Pollen Sprite</gradient></bold>")
    override val bossType: EntityType = EntityType.BEE
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.PINK, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 80.0,
            Attribute.FLYING_SPEED to 0.6,
            Attribute.MOVEMENT_SPEED to 0.35,
        )

    override fun onTick(entity: LivingEntity) {
        // Spawn angry bees every 6 seconds (120 ticks) if health < 50%
        if (entity.ticksLived % 120 != 0) return
        if (entity.health / (
                entity.getAttribute(Attribute.MAX_HEALTH)?.value
                    ?: 80.0
            ) > 0.5
        ) {
            return
        }

        repeat(3) {
            val bee =
                entity.world.spawnEntity(
                    entity.location.add(
                        (Random.nextDouble() - 0.5) * 4,
                        1.0,
                        (Random.nextDouble() - 0.5) * 4,
                    ),
                    EntityType.BEE,
                ) as Bee
            bee.anger = 600
            bee.target = entity.world.getNearbyPlayers(entity.location, 20.0).randomOrNull()
        }
        entity.world.spawnParticle(Particle.HAPPY_VILLAGER, entity.location, 20, 2.0, 1.0, 2.0, 0.0)
    }
}
