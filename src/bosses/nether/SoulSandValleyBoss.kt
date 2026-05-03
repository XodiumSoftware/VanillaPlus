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
 * A skeletal necromancer that commands the souls of the valley.
 */
internal object SoulSandValleyBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#F5F5DC:#696969>Osseus, the Soul Reaper</gradient></bold>")
    override val bossType: EntityType = EntityType.WITHER_SKELETON
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 350.0,
            Attribute.ATTACK_DAMAGE to 11.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.5,
        )

    override fun onTick(entity: LivingEntity) {
        // Summon skeleton reinforcements
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Heals when standing on soul sand or soul soil
    }
}
