package org.xodium.illyriaplus.bosses.end

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.block.Biome
import org.bukkit.entity.Endermite
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface
import kotlin.random.Random

/**
 * An enderman archmage that roams the midland slopes of the end islands.
 */
internal object EndMidlandsBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#9932CC:#8A2BE2>Voidwalker, the Rift Mage</gradient></bold>")
    override val bossType: EntityType = EntityType.ENDERMITE
    override val biome: Biome = Biome.END_MIDLANDS
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 200.0,
            Attribute.MOVEMENT_SPEED to 0.35,
            Attribute.ATTACK_DAMAGE to 8.0,
            Attribute.SCALE to 2.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Summon endermites every 8 seconds (160 ticks)
        if (entity.ticksLived % 160 != 0) return

        repeat(3) {
            val endermite =
                entity.world.spawnEntity(
                    entity.location.add(
                        (Random.nextDouble() - 0.5) * 4,
                        0.0,
                        (Random.nextDouble() - 0.5) * 4,
                    ),
                    EntityType.ENDERMITE,
                ) as Endermite
            endermite.target = entity.world.getNearbyPlayers(entity.location, 20.0).randomOrNull()
        }
        entity.world.spawnParticle(Particle.PORTAL, entity.location, 25, 2.0, 1.0, 2.0, 0.1)
    }
}
