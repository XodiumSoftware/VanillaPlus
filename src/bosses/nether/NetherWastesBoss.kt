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
 * A monstrous ghast that haunts the barren nether wastes.
 */
internal object NetherWastesBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FF4500:#8B0000>Blight, the Ashen Horror</gradient></bold>")
    override val bossType: EntityType = EntityType.GHAST
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 300.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Explosive fireball barrage
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Immune to fire and lava
    }
}
