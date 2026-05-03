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
 * An enderman archmage that roams the midland slopes of the end islands.
 */
internal object EndMidlandsBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#9932CC:#8A2BE2>Voidwalker, the Rift Mage</gradient></bold>")
    override val bossType: EntityType = EntityType.ENDERMITE
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 200.0,
            Attribute.MOVEMENT_SPEED to 0.35,
            Attribute.ATTACK_DAMAGE to 8.0,
        )

    override fun onTick(entity: LivingEntity) {
        // Create void rifts that pull players
    }
}
