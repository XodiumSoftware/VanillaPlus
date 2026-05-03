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
 * A grotesque creature that lurks in the murky swamp waters.
 */
internal object SwampBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#556B2F:#8B4513>Morgath, the Bog Wraith</gradient></bold>")
    override val bossType: EntityType = EntityType.WITCH
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 160.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.4,
        )

    override fun onTick(entity: LivingEntity) {
        // Throw random potion every 6 seconds (120 ticks)
        if (entity.ticksLived % 120 != 0) return

        val target = entity.world.getNearbyPlayers(entity.location, 15.0).randomOrNull() ?: return
        val effects =
            listOf(
                PotionEffectType.POISON,
                PotionEffectType.SLOWNESS,
                PotionEffectType.WEAKNESS,
                PotionEffectType.BLINDNESS,
            )

        target.addPotionEffect(PotionEffect(effects.random(), 100, 0))
        entity.world.spawnParticle(Particle.WITCH, target.location, 20, 0.5, 1.0, 0.5, 0.0)
    }
}
