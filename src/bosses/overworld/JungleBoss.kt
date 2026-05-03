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
 * A ferocious beast that stalks the dense jungle canopy.
 */
internal object JungleBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#32CD32:#228B22>Kaa'tar, the Vine Tyrant</gradient></bold>")
    override val bossType: EntityType = EntityType.OCELOT
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 150.0,
            Attribute.MOVEMENT_SPEED to 0.4,
            Attribute.ATTACK_DAMAGE to 8.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Climbs vines and leaves automatically
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Reduced fall damage
    }
}
