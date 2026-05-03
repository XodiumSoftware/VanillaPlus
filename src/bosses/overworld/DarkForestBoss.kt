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
 * A cursed entity that haunts the dark forest.
 */
internal object DarkForestBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#4B0082:#000000>Nocturne, the Shadow Lurker</gradient></bold>")
    override val bossType: EntityType = EntityType.VEX
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 120.0,
            Attribute.MOVEMENT_SPEED to 0.45,
            Attribute.ATTACK_DAMAGE to 7.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Shadow clone ability, darkness effect
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Phases through blocks
    }
}
