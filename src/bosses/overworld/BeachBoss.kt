package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Turtle
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.interfaces.BossInterface
import org.xodium.illyriaplus.utils.Utils.MM

/**
 * A crab-like creature that guards the beach shores.
 */
internal object BeachBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#00CED1:#F4A460>Crustar, the Shore Guardian</gradient></bold>")
    override val bossType: EntityType = EntityType.TURTLE
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 150.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.8,
            Attribute.ARMOR to 10.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Shell spin attack on sand
        if (entity.ticksLived % 60 != 0) return

        val loc = entity.location

        if (loc.block.type.name
                .contains("SAND")
        ) {
            entity.world.spawnParticle(Particle.SWEEP_ATTACK, loc, 20, 2.0, 0.5, 2.0)
            entity.world.getNearbyLivingEntities(loc, 3.0).filter { it !is Turtle }.forEach {
                it.damage(4.0, entity)
                it.velocity =
                    it.velocity.add(
                        it.location
                            .subtract(loc)
                            .toVector()
                            .normalize()
                            .multiply(0.5),
                    )
            }
        }
    }
}
