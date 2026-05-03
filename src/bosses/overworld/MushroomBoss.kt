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
 * A fungal entity that spreads spores across the mushroom island.
 */
internal object MushroomBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FF69B4:#DC143C>Mycelia, the Spore Queen</gradient></bold>")
    override val bossType: EntityType = EntityType.MOOSHROOM
    override val bossBar: BossBar =
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.PINK, BossBar.Overlay.PROGRESS)
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 250.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.6,
        )

    override fun onTick(entity: LivingEntity) {
        // Spreads mycelium, spawns mini mooshrooms
    }
}
