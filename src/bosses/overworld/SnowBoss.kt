package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface

/**
 * An ice-themed boss that brings winter's fury.
 */
internal object SnowBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#E0FFFF:#00BFFF>Aurora, the Frostbinder</gradient></bold>")
    override val bossType: EntityType = EntityType.STRAY
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 200.0,
            Attribute.ATTACK_DAMAGE to 7.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Freeze water around boss every 4 seconds (80 ticks)
        if (entity.ticksLived % 80 != 0) return

        val center = entity.location.block

        for (x in -3..3) {
            for (z in -3..3) {
                val block = center.world.getBlockAt(center.x + x, center.y - 1, center.z + z)

                if (block.type == Material.WATER) block.type = Material.ICE
            }
        }

        entity.world.spawnParticle(Particle.SNOWFLAKE, entity.location, 30, 3.0, 2.0, 3.0, 0.0)
    }
}
