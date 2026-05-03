package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.interfaces.BossInterface
import org.xodium.illyriaplus.utils.Utils.MM

/**
 * A pollen spirit that thrives among the flowers.
 */
internal object FlowerForestBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FFB6C1:#FFD700:#FFF0F5>Flora, the Pollen Sprite</gradient></bold>")
    override val bossType: EntityType = EntityType.BEE
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.PINK, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 80.0,
            Attribute.FLYING_SPEED to 0.6,
            Attribute.MOVEMENT_SPEED to 0.35,
        )

    override fun onTick(entity: LivingEntity) {
        // Pollen cloud (blindness effect), rapid regeneration near flowers
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Split into smaller bees on death
    }
}
