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
import kotlin.random.Random

/**
 * A delicate spirit that guards the cherry blossom groves.
 */
internal object CherryGroveBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FFB6C1:#FF69B4>Sakura, the Blossom Spirit</gradient></bold>")
    override val bossType: EntityType = EntityType.FOX
    override val biome: Biome = Biome.CHERRY_GROVE
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.PINK, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 160.0,
            Attribute.MOVEMENT_SPEED to 0.45,
            Attribute.ATTACK_DAMAGE to 6.0,
            Attribute.SCALE to 1.3,
        )

    override fun onTick(entity: LivingEntity) {
        // Petal storm every 5 seconds (100 ticks) that blinds and slows nearby players
        if (entity.ticksLived % 100 != 0) return

        entity.world.spawnParticle(Particle.CHERRY_LEAVES, entity.location, 50, 5.0, 3.0, 5.0, 0.0)
        entity.world.getNearbyPlayers(entity.location, 8.0).forEach {
            it.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 60, 1))
            if (Random.nextDouble() < 0.3) {
                it.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 40, 0))
            }
        }
    }
}
