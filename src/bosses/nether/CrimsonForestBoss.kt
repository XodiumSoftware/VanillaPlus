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
 * A massive hoglin warlord that rules the crimson forests.
 */
internal object CrimsonForestBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#DC143C:#8B0000>Tuskarr, the Crimson Warlord</gradient></bold>")
    override val bossType: EntityType = EntityType.HOGLIN
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 400.0,
            Attribute.ATTACK_DAMAGE to 12.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.6,
        )

    override fun onTick(entity: LivingEntity) {
        // Summon hoglin reinforcements
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Enraged when near crimson nylium
    }
}
