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
import org.xodium.illyriaplus.interfaces.BossInterface
import org.xodium.illyriaplus.utils.Utils.MM

/**
 * A ferocious beast that stalks the dense jungle canopy.
 */
internal object JungleBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#32CD32:#228B22>Kaa'tar, the Vine Tyrant</gradient></bold>")
    override val bossType: EntityType = EntityType.OCELOT
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 150.0,
            Attribute.MOVEMENT_SPEED to 0.4,
            Attribute.ATTACK_DAMAGE to 8.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Speed boost and pounce to nearby players every 4 seconds (80 ticks)
        if (entity.ticksLived % 80 != 0) return

        entity.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 60, 1))

        val target = entity.world.getNearbyPlayers(entity.location, 15.0).randomOrNull() ?: return
        // Pounce toward target
        val direction =
            target.location
                .subtract(entity.location)
                .toVector()
                .normalize()

        entity.velocity = direction.multiply(1.5).setY(0.5)
        entity.world.spawnParticle(Particle.CLOUD, entity.location, 15, 0.3, 0.1, 0.3, 0.05)
    }
}
