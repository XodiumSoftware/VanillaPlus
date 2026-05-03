package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.block.Biome
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface

/**
 * A pharaoh-like boss that rules the scorching desert sands.
 */
internal object DesertBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FFD700:#FF8C00>Anubis, the Sand Pharaoh</gradient></bold>")
    override val bossType: EntityType = EntityType.HUSK
    override val biome: Biome = Biome.DESERT
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 250.0,
            Attribute.ATTACK_DAMAGE to 10.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.5,
            Attribute.SCALE to 1.8,
        )

    override fun onTick(entity: LivingEntity) {
        // Sandstorm particles + slowness to nearby every 4 seconds (80 ticks)
        if (entity.ticksLived % 80 != 0) return

        entity.world.spawnParticle(
            Particle.FALLING_DUST,
            entity.location,
            30,
            3.0,
            2.0,
            3.0,
            0.0,
            Material.SAND.createBlockData(),
        )
        entity.world.getNearbyPlayers(entity.location, 10.0).forEach {
            it.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 60, 1))
        }
    }
}
