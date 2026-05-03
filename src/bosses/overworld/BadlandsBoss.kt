package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface

/**
 * A bandit king that controls the badlands territory.
 */
internal object BadlandsBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#CD5C5C:#8B4513>Rattlesnake, the Mesa Marauder</gradient></bold>")
    override val bossType: EntityType = EntityType.PILLAGER
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 180.0,
            Attribute.ATTACK_DAMAGE to 8.0,
            Attribute.MOVEMENT_SPEED to 0.35,
            Attribute.SCALE to 1.4,
        )

    override fun onTick(entity: LivingEntity) {
        // Rapid fire crossbow attacks at nearby players
        if (entity.ticksLived % 40 != 0) return

        entity.world.getNearbyPlayers(entity.location, 20.0).randomOrNull()?.let {
            entity.world
                .spawnArrow(
                    entity.eyeLocation,
                    it.eyeLocation
                        .subtract(entity.eyeLocation)
                        .toVector()
                        .normalize()
                        .multiply(2.0),
                    1.5f,
                    5f,
                ).apply {
                    shooter = entity
                    damage = 6.0
                }
        }
    }
}
