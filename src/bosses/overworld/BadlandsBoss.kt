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
        )

    override fun onTick(entity: LivingEntity) {
        // Rapid fire crossbow attacks
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Increased damage when on gold ore
    }
}
