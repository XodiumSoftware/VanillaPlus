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
 * An enderman shaman that channels the warped energy of the forest.
 */
internal object WarpedForestBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#00CED1:#191970>Vexis, the Warped Seer</gradient></bold>")
    override val bossType: EntityType = EntityType.ENDERMAN
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 250.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Teleport behind players, apply blindness
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Warp away when hit by projectiles
    }
}
