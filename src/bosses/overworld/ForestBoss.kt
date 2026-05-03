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
        )

    override fun onTick(entity: LivingEntity) {
        // Regenerate health when near leaves/wood
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Handle damage events
    }
}
