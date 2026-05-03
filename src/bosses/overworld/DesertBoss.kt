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
 * A pharaoh-like boss that rules the scorching desert sands.
 */
internal object DesertBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FFD700:#FF8C00>Anubis, the Sand Pharaoh</gradient></bold>")
    override val bossType: EntityType = EntityType.HUSK
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 250.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Apply hunger effect to nearby players
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Immune to fire damage
    }
}
