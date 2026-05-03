package org.xodium.illyriaplus.bosses.nether

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
 * A magma golem forged in the volcanic basalt deltas.
 */
internal object BasaltDeltasBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FF6347:#2F4F4F>Magmatus, the Basalt Colossus</gradient></bold>")
    override val bossType: EntityType = EntityType.MAGMA_CUBE
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 450.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.8,
            Attribute.ARMOR to 12.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Earthquake stomp, summon magma cubes
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Immune to fire damage, heals in lava
    }
}
