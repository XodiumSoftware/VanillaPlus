package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Material
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
 * A mighty guardian of the cold taiga forests.
 */
internal object TaigaBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#00CED1:#4682B4>Bjorn, the Frost Sentinel</gradient></bold>")
    override val bossType: EntityType = EntityType.POLAR_BEAR
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 300.0,
            Attribute.ATTACK_DAMAGE to 12.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.7,
        )

    override fun onTick(entity: LivingEntity) {
        // Frost trail that slows enemies every 3 seconds (60 ticks)
        if (entity.ticksLived % 60 != 0) return

        val block = entity.location.subtract(0.0, 1.0, 0.0).block

        if (block.type == Material.GRASS_BLOCK || block.type == Material.DIRT || block.type == Material.PODZOL) {
            block.type = Material.SNOW
        }

        entity.world.getNearbyLivingEntities(entity.location, 5.0).filter { it != entity }.forEach {
            it.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 40, 1))
        }
        entity.world.spawnParticle(Particle.SNOWFLAKE, entity.location, 15, 1.0, 0.5, 1.0, 0.0)
    }
}
