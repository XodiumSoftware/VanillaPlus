package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
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
 * A leviathan that rules the ocean depths.
 */
internal object OceanBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#1E90FF:#00008B>Leviathan, the Abyssal Tyrant</gradient></bold>")
    override val bossType: EntityType = EntityType.GUARDIAN
    override val biome: Biome = Biome.OCEAN
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 400.0,
            Attribute.ATTACK_DAMAGE to 12.0,
            Attribute.MOVEMENT_SPEED to 0.3,
            Attribute.SCALE to 2.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Beam targeting random player every 3 seconds (60 ticks)
        if (entity.ticksLived % 60 != 0) return

        val target = entity.world.getNearbyPlayers(entity.location, 25.0).randomOrNull() ?: return

        target.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 40, 0))
        target.damage(4.0, entity)

        entity.world.spawnParticle(Particle.BUBBLE, target.location, 20, 0.5, 1.0, 0.5, 0.0)
    }
}
