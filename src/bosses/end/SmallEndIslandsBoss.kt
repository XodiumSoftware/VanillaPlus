package org.xodium.illyriaplus.bosses.end

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.interfaces.BossInterface
import org.xodium.illyriaplus.utils.Utils.MM

/**
 * A spectral guardian that protects the small floating end islands.
 */
internal object SmallEndIslandsBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#00CED1:#5F9EA0>Aetherion, the Star Warden</gradient></bold>")
    override val bossType: EntityType = EntityType.ALLAY
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 180.0,
            Attribute.FLYING_SPEED to 0.6,
        )

    override fun onTick(entity: LivingEntity) {
        // Gravity manipulation, push/pull players
    }
}
