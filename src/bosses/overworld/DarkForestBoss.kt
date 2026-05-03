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
 * A cursed entity that haunts the dark forest.
 */
internal object DarkForestBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#4B0082:#000000>Nocturne, the Shadow Lurker</gradient></bold>")
    override val bossType: EntityType = EntityType.VEX
    override val biome: Biome = Biome.DARK_FOREST
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 120.0,
            Attribute.MOVEMENT_SPEED to 0.45,
            Attribute.ATTACK_DAMAGE to 7.0,
            Attribute.SCALE to 1.3,
        )

    override fun onTick(entity: LivingEntity) {
        // Teleport behind players every 5 seconds (100 ticks)
        if (entity.ticksLived % 100 != 0) return

        val target = entity.world.getNearbyPlayers(entity.location, 15.0).randomOrNull() ?: return
        val behind = target.location.subtract(target.location.direction.multiply(2))

        behind.y = target.location.y
        entity.teleport(behind)
        target.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 100, 0))
        entity.world.spawnParticle(Particle.PORTAL, entity.location, 20, 0.3, 0.3, 0.3, 0.1)
    }
}
