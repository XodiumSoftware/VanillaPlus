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
 * A stone giant that dwells in the mountain peaks.
 */
internal object MountainBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#D3D3D3:#696969>Titan, the Peak Colossus</gradient></bold>")
    override val bossType: EntityType = EntityType.IRON_GOLEM
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 500.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Stomp creates shockwaves, throw boulders
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Huge fall damage resistance
    }
}
