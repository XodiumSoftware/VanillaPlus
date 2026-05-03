package org.xodium.illyriaplus.bosses.end

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.IllyriaPlus.Companion.instance
import org.xodium.illyriaplus.interfaces.BossInterface
import org.xodium.illyriaplus.utils.Utils.MM

/**
 * A corrupted phantom that haunts the desolate end barrens.
 */
internal object EndBarrensBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#2F4F4F:#000000>Umbrath, the Void Stalker</gradient></bold>")
    override val bossType: EntityType = EntityType.PHANTOM
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 220.0,
            Attribute.FLYING_SPEED to 0.5,
            Attribute.MOVEMENT_SPEED to 0.35,
        )

    override fun onTick(entity: LivingEntity) {
        // Swoop attack from above every 5 seconds (100 ticks)
        if (entity.ticksLived % 100 != 0) return

        val target = entity.world.getNearbyPlayers(entity.location, 25.0).randomOrNull() ?: return
        entity.teleport(target.location.add(0.0, 8.0, 0.0))

        // Dive down at target
        instance.server.scheduler.runTaskLater(
            instance,
            Runnable {
                entity.velocity =
                    target.location
                        .subtract(entity.location)
                        .toVector()
                        .normalize()
                        .multiply(2.0)
                        .setY(-1.0)
            },
            10L,
        )

        entity.world
            .getNearbyLivingEntities(entity.location, 3.0)
            .filter { it != entity && it is Player }
            .forEach { it.damage(8.0, entity) }
        entity.world.spawnParticle(Particle.SWEEP_ATTACK, entity.location, 10, 1.0, 0.5, 1.0, 0.0)
    }
}
