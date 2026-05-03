package org.xodium.illyriaplus.bosses.overworld

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
 * A fungal entity that spreads spores across the mushroom island.
 */
internal object MushroomBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FF69B4:#DC143C>Mycelia, the Spore Queen</gradient></bold>")
    override val bossType: EntityType = EntityType.MOOSHROOM
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.PINK, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 250.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.6,
        )

    override fun onTick(entity: LivingEntity) {
        // Spore particles + nausea to nearby every 4 seconds (80 ticks)
        if (entity.ticksLived % 80 != 0) return

        entity.world.spawnParticle(Particle.SPORE_BLOSSOM_AIR, entity.location, 30, 3.0, 2.0, 3.0, 0.0)
        entity.world.getNearbyPlayers(entity.location, 8.0).forEach {
            it.addPotionEffect(PotionEffect(PotionEffectType.NAUSEA, 100, 0))
        }
    }
}
