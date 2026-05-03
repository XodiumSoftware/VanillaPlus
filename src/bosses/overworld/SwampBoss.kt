package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.xodium.illyriaplus.interfaces.BossInterface
import org.xodium.illyriaplus.utils.Utils.MM

/**
 * A grotesque creature that lurks in the murky swamp waters.
 */
internal object SwampBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#556B2F:#8B4513>Morgath, the Bog Wraith</gradient></bold>")
    override val bossType: EntityType = EntityType.WITCH
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 160.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.4,
        )

    override fun onTick(entity: LivingEntity) {
        // Spawn slimes, apply poison to nearby players
    }
}
