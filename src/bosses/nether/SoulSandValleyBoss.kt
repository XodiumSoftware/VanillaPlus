package org.xodium.illyriaplus.bosses.nether

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.block.Biome
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface

/**
 * A skeletal necromancer that commands the souls of the valley.
 */
internal object SoulSandValleyBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#F5F5DC:#696969>Osseus, the Soul Reaper</gradient></bold>")
    override val bossType: EntityType = EntityType.WITHER_SKELETON
    override val biome: Biome = Biome.SOUL_SAND_VALLEY
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 350.0,
            Attribute.ATTACK_DAMAGE to 11.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.5,
            Attribute.SCALE to 1.7,
        )

    override fun onTick(entity: LivingEntity) {
        // Wither effect to nearby every 4 seconds (80 ticks)
        if (entity.ticksLived % 80 != 0) return

        entity.world.spawnParticle(Particle.SOUL, entity.location, 30, 4.0, 2.0, 4.0, 0.0)
        entity.world
            .getNearbyLivingEntities(entity.location, 10.0)
            .filter { it != entity && it is Player }
            .forEach { it.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 60, 1)) }
    }
}
