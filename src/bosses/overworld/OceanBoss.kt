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
 * A leviathan that rules the ocean depths.
 */
internal object OceanBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#1E90FF:#00008B>Leviathan, the Abyssal Tyrant</gradient></bold>")
    override val bossType: EntityType = EntityType.GUARDIAN
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 400.0,
            Attribute.ATTACK_DAMAGE to 12.0,
            Attribute.MOVEMENT_SPEED to 0.3,
        )

    override fun onTick(entity: LivingEntity) {
        // Mining fatigue aura, summon drowned
    }
}
