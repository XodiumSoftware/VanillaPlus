package org.xodium.illyriaplus.bosses.end

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface

/**
 * A crystalline golem that guards the towering end highlands pillars.
 */
internal object EndHighlandsBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#9400D3:#4B0082>Prismar, the Crystal Sentinel</gradient></bold>")
    override val bossType: EntityType = EntityType.SHULKER
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 280.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.9,
            Attribute.ARMOR to 10.0,
            Attribute.SCALE to 1.4,
        )

    override fun onTick(entity: LivingEntity) {
        // Levitation bullets every 4 seconds (80 ticks)
        if (entity.ticksLived % 80 != 0) return

        val target = entity.world.getNearbyPlayers(entity.location, 20.0).randomOrNull() ?: return
        target.addPotionEffect(PotionEffect(PotionEffectType.LEVITATION, 60, 0))
        entity.world.spawnParticle(Particle.END_ROD, target.location, 20, 0.5, 1.0, 0.5, 0.0)
    }
}
