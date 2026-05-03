package org.xodium.illyriaplus.bosses.nether

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.block.Biome
import org.bukkit.entity.EntityType
import org.bukkit.entity.Hoglin
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface
import kotlin.random.Random

/**
 * A massive hoglin warlord that rules the crimson forests.
 */
internal object CrimsonForestBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#DC143C:#8B0000>Tuskarr, the Crimson Warlord</gradient></bold>")
    override val bossType: EntityType = EntityType.HOGLIN
    override val biome: Biome = Biome.CRIMSON_FOREST
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 400.0,
            Attribute.ATTACK_DAMAGE to 12.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.6,
            Attribute.SCALE to 1.9,
        )

    override fun onTick(entity: LivingEntity) {
        // Summon 2 hoglins every 10 seconds (200 ticks) if health < 60%
        if (entity.ticksLived % 200 != 0) return
        if (entity.health / (entity.getAttribute(Attribute.MAX_HEALTH)?.value ?: 400.0) > 0.6) return

        repeat(2) {
            val hoglin =
                entity.world.spawnEntity(
                    entity.location.add(
                        (Random.nextDouble() - 0.5) * 4,
                        0.0,
                        (Random.nextDouble() - 0.5) * 4,
                    ),
                    EntityType.HOGLIN,
                ) as Hoglin
            hoglin.target = entity.world.getNearbyPlayers(entity.location, 25.0).randomOrNull()
        }
        entity.world.spawnParticle(Particle.CRIMSON_SPORE, entity.location, 30, 2.0, 1.0, 2.0, 0.0)
    }
}
