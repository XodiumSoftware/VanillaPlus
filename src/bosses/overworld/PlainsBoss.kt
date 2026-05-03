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
 * A boss that roams the plains, commanding the wind and grass.
 */
internal object PlainsBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FFD700:#FFA500>Zephyr, the Tempest Walker</gradient></bold>")
    override val bossType: EntityType = EntityType.ZOMBIE
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 200.0,
            Attribute.ATTACK_DAMAGE to 8.0,
            Attribute.MOVEMENT_SPEED to 0.3,
        )

    override fun onTick(entity: LivingEntity) {
        // Passive wind aura that pushes nearby players
    }
}
