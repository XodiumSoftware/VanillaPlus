package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Zombie
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface
import kotlin.random.Random

/**
 * A boss that roams the plains, commanding the wind and grass.
 */
internal object PlainsBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FFD700:#FFA500>Zephyr, the Tempest Walker</gradient></bold>")
    override val bossType: EntityType = EntityType.ZOMBIE
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 200.0,
            Attribute.ATTACK_DAMAGE to 8.0,
            Attribute.MOVEMENT_SPEED to 0.3,
        )

    override fun onTick(entity: LivingEntity) {
        // Summon zombie reinforcements every 8 seconds (160 ticks) if health < 50%
        if (entity.ticksLived % 160 != 0) return
        if (entity.health / (entity.getAttribute(Attribute.MAX_HEALTH)?.value ?: 200.0) > 0.5) return

        repeat(2) {
            val zombie =
                entity.world.spawnEntity(
                    entity.location.add(
                        (Random.nextDouble() - 0.5) * 6,
                        0.0,
                        (Random.nextDouble() - 0.5) * 6,
                    ),
                    EntityType.ZOMBIE,
                ) as Zombie

            zombie.target = entity.world.getNearbyPlayers(entity.location, 20.0).randomOrNull()
        }
        entity.world.spawnParticle(Particle.SCULK_SOUL, entity.location, 20, 2.0, 1.0, 2.0, 0.0)
    }
}
