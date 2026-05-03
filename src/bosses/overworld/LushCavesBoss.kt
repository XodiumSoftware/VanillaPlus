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
 * A fungal guardian that thrives in the lush cave depths.
 */
internal object LushCavesBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#00FF7F:#20B2AA>Verdania, the Lush Keeper</gradient></bold>")
    override val bossType: EntityType = EntityType.AXOLOTL
    override val biome: Biome = Biome.LUSH_CAVES
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 180.0,
            Attribute.MOVEMENT_SPEED to 0.35,
            Attribute.ATTACK_DAMAGE to 7.0,
            Attribute.SCALE to 2.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Heal in water and glowing spores every 4 seconds (80 ticks)
        if (entity.ticksLived % 80 != 0) return

        // Check if in water
        if (entity.location.block.type.name
                .contains("WATER")
        ) {
            entity.heal(8.0)
        }

        // Glow and regeneration to boss
        entity.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 60, 0))
        entity.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 100, 0))

        // Random effect to nearby players
        entity.world.getNearbyPlayers(entity.location, 10.0).forEach { player ->
            val effect = listOf(PotionEffectType.POISON, PotionEffectType.SLOWNESS, PotionEffectType.WEAKNESS).random()
            if (Random.nextDouble() < 0.4) {
                player.addPotionEffect(PotionEffect(effect, 60, 0))
            }
        }

        entity.world.spawnParticle(Particle.HAPPY_VILLAGER, entity.location, 25, 3.0, 1.5, 3.0, 0.0)
    }
}
