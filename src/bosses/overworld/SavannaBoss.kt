package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface

/**
 * A swift warrior of the savanna grasslands.
 */
internal object SavannaBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#F0E68C:#D2691E>Khan, the Grassland Raider</gradient></bold>")
    override val bossType: EntityType = EntityType.HORSE
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 220.0,
            Attribute.MOVEMENT_SPEED to 0.4,
            Attribute.ATTACK_DAMAGE to 9.0,
            Attribute.SCALE to 1.5,
        )

    override fun onTick(entity: LivingEntity) {
        // Charge attack that deals extra damage every 5 seconds (100 ticks)
        if (entity.ticksLived % 100 != 0) return

        val target = entity.world.getNearbyPlayers(entity.location, 20.0).randomOrNull() ?: return

        entity.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 40, 2))

        // Charge toward target
        val direction =
            target.location
                .subtract(entity.location)
                .toVector()
                .normalize()

        entity.velocity = direction.multiply(2.0)
        // Damage nearby entities after charge
        entity.world
            .getNearbyLivingEntities(entity.location, 3.0)
            .filter { it != entity && it is Player }
            .forEach { it.damage(10.0, entity) }
        entity.world.spawnParticle(Particle.CLOUD, entity.location, 20, 1.0, 0.5, 1.0, 0.1)
    }
}
