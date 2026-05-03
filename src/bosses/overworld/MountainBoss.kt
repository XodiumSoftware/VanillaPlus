package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface

/**
 * A stone giant that dwells in the mountain peaks.
 */
internal object MountainBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#D3D3D3:#696969>Titan, the Peak Colossus</gradient></bold>")
    override val bossType: EntityType = EntityType.IRON_GOLEM
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 500.0,
            Attribute.KNOCKBACK_RESISTANCE to 1.0,
            Attribute.ATTACK_DAMAGE to 15.0,
            Attribute.ARMOR to 15.0,
            Attribute.SCALE to 2.5,
        )

    override fun onTick(entity: LivingEntity) {
        // Shockwave knockback every 5 seconds (100 ticks)
        if (entity.ticksLived % 100 != 0) return

        entity.world.spawnParticle(Particle.EXPLOSION, entity.location, 5, 2.0, 0.5, 2.0, 0.0)
        entity.world.getNearbyLivingEntities(entity.location, 8.0).filter { it != entity }.forEach {
            val direction =
                it.location
                    .subtract(entity.location)
                    .toVector()
                    .normalize()

            it.velocity = direction.multiply(1.5).setY(0.8)
            it.damage(6.0, entity)
        }
        entity.world.playSound(entity.location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f)
    }
}
