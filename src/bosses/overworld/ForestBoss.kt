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
 * A boss that lurks within the dense forest canopy.
 */
internal object ForestBoss : BossInterface {
    override val name: String = "Thornheart, the Canopy Stalker"
    override val entityType: EntityType = EntityType.SKELETON
    override val bossMaxHealth: Double = 180.0
    override val drops: List<ItemStack> get() = emptyList()

    override val bossBar: BossBar =
        BossBar.bossBar(
            MM.deserialize("<bold><gradient:#228B22:#006400>$name</gradient></bold>"),
            1.0f,
            BossBar.Color.GREEN,
            BossBar.Overlay.PROGRESS,
        )

    override fun spawn(location: Location): LivingEntity =
        (location.world.spawnEntity(location, entityType) as LivingEntity).apply {
            customName(MM.deserialize("<bold><gradient:#228B22:#006400>$name</gradient></bold>"))
            isCustomNameVisible = true
            health = bossMaxHealth
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = bossMaxHealth
            addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, Int.MAX_VALUE, 0, false, false))
        }

    override fun despawn(entity: LivingEntity) {
        entity.remove()
    }

    override fun onDeath(entity: LivingEntity) {
        // Can drop forest-themed items
    }

    override fun onDamage(
        entity: LivingEntity,
        damage: Double,
    ) {
        // Chance to entangle attacker in roots
    }

    override fun onTick(entity: LivingEntity) {
        // Regenerate health when near leaves/wood
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Handle damage events
    }
}
