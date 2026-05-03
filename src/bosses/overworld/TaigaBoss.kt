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
 * A mighty guardian of the cold taiga forests.
 */
internal object TaigaBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#00CED1:#4682B4>Bjorn, the Frost Sentinel</gradient></bold>")
    override val bossType: EntityType = EntityType.POLAR_BEAR
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 300.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Leaves frosted trail, slow nearby enemies
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Immune to freezing damage
    }
}
