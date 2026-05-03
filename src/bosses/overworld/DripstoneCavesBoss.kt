package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.block.Biome
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface

/**
 * A stalwart guardian formed from the cave dripstone.
 */
internal object DripstoneCavesBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#8B7355:#DEB887>Stalagmus, the Stone Sentinel</gradient></bold>")
    override val bossType: EntityType = EntityType.SILVERFISH
    override val biome: Biome = Biome.DRIPSTONE_CAVES
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 280.0,
            Attribute.ATTACK_DAMAGE to 11.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.8,
            Attribute.ARMOR to 12.0,
            Attribute.SCALE to 1.6,
        )

    override fun onTick(entity: LivingEntity) {
        // Falling stalactite damage every 4 seconds (80 ticks)
        if (entity.ticksLived % 80 != 0) return

        val target = entity.world.getNearbyPlayers(entity.location, 15.0).randomOrNull() ?: return

        // Simulate falling dripstone
        entity.world.spawnParticle(
            Particle.FALLING_DUST,
            target.location.add(0.0, 5.0, 0.0),
            20,
            0.5,
            0.5,
            0.5,
            0.0,
            Material.POINTED_DRIPSTONE.createBlockData(),
        )

        // Damage target after delay
        target.damage(8.0, entity)
        target.velocity = target.velocity.setY(-0.3)

        entity.world.spawnParticle(Particle.DRIPPING_DRIPSTONE_WATER, entity.location, 30, 3.0, 2.0, 3.0, 0.0)
    }
}
