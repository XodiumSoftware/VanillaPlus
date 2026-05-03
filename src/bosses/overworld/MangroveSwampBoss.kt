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
 * A vengeful spirit that haunts the mangrove roots.
 */
internal object MangroveSwampBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#556B2F:#8FBC8F>Rootrot, the Mangrove Wraith</gradient></bold>")
    override val bossType: EntityType = EntityType.DROWNED
    override val biome: Biome = Biome.MANGROVE_SWAMP
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 200.0,
            Attribute.ATTACK_DAMAGE to 9.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.5,
            Attribute.SCALE to 1.4,
        )

    override fun onTick(entity: LivingEntity) {
        // Root grasp - pull players down every 5 seconds (100 ticks)
        if (entity.ticksLived % 100 != 0) return

        entity.world.spawnParticle(Particle.SPORE_BLOSSOM_AIR, entity.location, 40, 4.0, 2.0, 4.0, 0.0)
        entity.world.getNearbyPlayers(entity.location, 12.0).forEach {
            it.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 80, 2))
            it.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 60, 0))
            // Pull down slightly
            it.velocity = it.velocity.setY(-0.5)
        }
    }
}
