package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Arrow
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface

/**
 * A graceful archer that guards the birch groves.
 */
internal object BirchForestBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FFFAF0:#F0E68C>Sylvara, the White Huntress</gradient></bold>")
    override val bossType: EntityType = EntityType.SKELETON
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 140.0,
            Attribute.MOVEMENT_SPEED to 0.4,
            Attribute.ATTACK_DAMAGE to 6.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Arrow barrage every 3 seconds (60 ticks)
        if (entity.ticksLived % 60 != 0) return

        val target = entity.world.getNearbyPlayers(entity.location, 20.0).randomOrNull() ?: return
        val arrow = entity.launchProjectile(Arrow::class.java)

        arrow.velocity =
            target.location
                .subtract(entity.location)
                .toVector()
                .normalize()
                .multiply(2.5)
        entity.world.spawnParticle(Particle.CRIT, entity.location, 15, 0.5, 0.5, 0.5, 0.1)
    }
}
