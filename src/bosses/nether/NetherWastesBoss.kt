package org.xodium.illyriaplus.bosses.nether

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Fireball
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface

/**
 * A monstrous ghast that haunts the barren nether wastes.
 */
internal object NetherWastesBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FF4500:#8B0000>Blight, the Ashen Horror</gradient></bold>")
    override val bossType: EntityType = EntityType.GHAST
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 300.0,
            Attribute.FLYING_SPEED to 0.5,
        )

    override fun onTick(entity: LivingEntity) {
        // Fireball every 4 seconds (80 ticks) at nearby players
        if (entity.ticksLived % 80 != 0) return

        val target = entity.world.getNearbyPlayers(entity.location, 30.0).randomOrNull() ?: return
        val fireball = entity.launchProjectile(Fireball::class.java)
        fireball.velocity =
            target.location
                .subtract(entity.location)
                .toVector()
                .normalize()
                .multiply(1.5)
        entity.world.spawnParticle(Particle.FLAME, entity.location, 20, 0.5, 0.5, 0.5, 0.1)
    }
}
