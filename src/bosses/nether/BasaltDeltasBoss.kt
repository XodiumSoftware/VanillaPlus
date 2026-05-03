package org.xodium.illyriaplus.bosses.nether

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface

/**
 * A magma golem forged in the volcanic basalt deltas.
 */
internal object BasaltDeltasBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FF6347:#2F4F4F>Magmatus, the Basalt Colossus</gradient></bold>")
    override val bossType: EntityType = EntityType.MAGMA_CUBE
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 450.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.8,
            Attribute.ARMOR to 12.0,
            Attribute.SCALE to 2.2,
        )

    override fun onTick(entity: LivingEntity) {
        // Lava particles + fire to nearby every 3 seconds (60 ticks)
        if (entity.ticksLived % 60 != 0) return

        entity.world.spawnParticle(Particle.LAVA, entity.location, 20, 2.0, 1.0, 2.0, 0.0)
        entity.world.getNearbyLivingEntities(entity.location, 6.0).filter { it != entity }.forEach {
            it.fireTicks = 60
            it.damage(3.0, entity)
        }
    }
}
