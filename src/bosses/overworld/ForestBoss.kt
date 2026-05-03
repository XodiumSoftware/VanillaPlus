package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.interfaces.BossInterface
import org.xodium.illyriaplus.utils.Utils.MM

/**
 * A boss that lurks within the dense forest canopy.
 */
internal object ForestBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#228B22:#006400>Thornheart, the Canopy Stalker</gradient></bold>")
    override val bossType: EntityType = EntityType.SKELETON
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 180.0,
            Attribute.ATTACK_DAMAGE to 7.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Heal when near leaves/logs every 2 seconds (40 ticks)
        if (entity.ticksLived % 40 != 0) return

        val nearbyBlocks =
            listOf(
                entity.location
                    .add(1.0, 0.0, 0.0)
                    .block.type,
                entity.location
                    .add(-1.0, 0.0, 0.0)
                    .block.type,
                entity.location
                    .add(0.0, 1.0, 0.0)
                    .block.type,
                entity.location
                    .add(0.0, -1.0, 0.0)
                    .block.type,
                entity.location
                    .add(0.0, 0.0, 1.0)
                    .block.type,
                entity.location
                    .add(0.0, 0.0, -1.0)
                    .block.type,
            )

        val woodTypes =
            listOf(
                Material.OAK_LOG,
                Material.OAK_LEAVES,
                Material.BIRCH_LOG,
                Material.BIRCH_LEAVES,
                Material.SPRUCE_LOG,
                Material.SPRUCE_LEAVES,
                Material.JUNGLE_LOG,
                Material.JUNGLE_LEAVES,
                Material.ACACIA_LOG,
                Material.ACACIA_LEAVES,
                Material.DARK_OAK_LOG,
                Material.DARK_OAK_LEAVES,
                Material.MANGROVE_LOG,
                Material.MANGROVE_LEAVES,
                Material.CHERRY_LOG,
                Material.CHERRY_LEAVES,
            )

        if (nearbyBlocks.any { it in woodTypes }) {
            entity.heal(4.0)
            entity.world.spawnParticle(
                Particle.HAPPY_VILLAGER,
                entity.location.add(0.0, 1.0, 0.0),
                10,
                0.5,
                0.5,
                0.5,
                0.0,
            )
        }
    }
}
