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
 * A haunting entity that lurks in the pale oak gardens.
 */
internal object PaleGardenBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#808080:#2F2F2F>Creak, the Pale Watcher</gradient></bold>")
    override val bossType: EntityType = EntityType.WARDEN
    override val biome: Biome = Biome.PALE_GARDEN
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 500.0,
            Attribute.ATTACK_DAMAGE to 15.0,
            Attribute.KNOCKBACK_RESISTANCE to 1.0,
            Attribute.ARMOR to 15.0,
            Attribute.SCALE to 1.2,
        )

    override fun onTick(entity: LivingEntity) {
        // Darkness pulse every 6 seconds (120 ticks)
        if (entity.ticksLived % 120 != 0) return

        entity.world.spawnParticle(Particle.SCULK_SOUL, entity.location, 30, 4.0, 2.0, 4.0, 0.0)
        entity.world.getNearbyPlayers(entity.location, 15.0).forEach {
            it.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 100, 0))
            it.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 60, 2))
        }
    }
}
