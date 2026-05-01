package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.xodium.illyriaplus.interfaces.BossInterface
import org.xodium.illyriaplus.utils.Utils.MM

/**
 * A crab-like creature that guards the beach shores.
 */
internal object BeachBoss : BossInterface {
    override val name: String = "Crustar, the Shore Guardian"
    override val entityType: EntityType = EntityType.TURTLE
    override val bossMaxHealth: Double = 150.0
    override val drops: List<ItemStack> get() = emptyList()

    override val bossBar: BossBar =
        BossBar.bossBar(
            MM.deserialize("<bold><gradient:#00CED1:#F4A460>$name</gradient></bold>"),
            1.0f,
            BossBar.Color.BLUE,
            BossBar.Overlay.PROGRESS,
        )

    override fun spawn(location: Location): LivingEntity =
        (location.world.spawnEntity(location, entityType) as LivingEntity).apply {
            customName(MM.deserialize("<bold><gradient:#00CED1:#F4A460>$name</gradient></bold>"))
            isCustomNameVisible = true
            health = bossMaxHealth
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = bossMaxHealth
            addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, Int.MAX_VALUE, 1, false, false))
        }

    override fun despawn(entity: LivingEntity) {
        entity.remove()
    }

    override fun onDeath(entity: LivingEntity) {
        // Scute, turtle shell drops
    }

    override fun onDamage(
        entity: LivingEntity,
        damage: Double,
    ) {
        // Retract and resist damage
    }

    override fun onTick(entity: LivingEntity) {
        // Shell spin attack on sand
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // High resistance on sand blocks
    }
}
