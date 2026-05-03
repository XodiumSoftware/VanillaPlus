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
 * A corrupted phantom that haunts the desolate end barrens.
 */
internal object EndBarrensBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#2F4F4F:#000000>Umbrath, the Void Stalker</gradient></bold>")
    override val bossType: EntityType = EntityType.PHANTOM
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 220.0,
            Attribute.FLYING_SPEED to 0.5,
            Attribute.MOVEMENT_SPEED to 0.35,
        )

    override fun onTick(entity: LivingEntity) {
        // Invisibility in shadows, swoop attacks
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Dodge attacks by phasing through blocks
    }
}
