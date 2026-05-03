package org.xodium.illyriaplus.bosses.end

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
 * A crystalline golem that guards the towering end highlands pillars.
 */
internal object EndHighlandsBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#9400D3:#4B0082>Prismar, the Crystal Sentinel</gradient></bold>")
    override val bossType: EntityType = EntityType.SHULKER
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 280.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.9,
            Attribute.ARMOR to 10.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Levitation field around the pillar
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Shell closes when health is low, granting immunity
    }
}
