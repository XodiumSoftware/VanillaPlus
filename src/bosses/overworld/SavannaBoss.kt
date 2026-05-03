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
 * A swift warrior of the savanna grasslands.
 */
internal object SavannaBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#F0E68C:#D2691E>Khan, the Grassland Raider</gradient></bold>")
    override val bossType: EntityType = EntityType.HORSE
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 220.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Rapid movement, trampling attack
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Increased speed when damaged
    }
}
