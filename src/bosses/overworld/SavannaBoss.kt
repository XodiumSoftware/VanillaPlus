package org.xodium.illyriaplus.bosses.overworld

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
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
 * A swift warrior of the savanna grasslands.
 */
internal object SavannaBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#F0E68C:#D2691E>Khan, the Grassland Raider</gradient></bold>")
    override val entityType: EntityType = EntityType.HORSE
    override val bossMaxHealth: Double = 220.0
    override val drops: List<ItemStack> get() = emptyList()

    override val bossBar: BossBar =
        BossBar.bossBar(
            bossName,
            1.0f,
            BossBar.Color.YELLOW,
            BossBar.Overlay.PROGRESS,
        )

    override fun spawn(location: Location): LivingEntity =
        (location.world.spawnEntity(location, entityType) as LivingEntity).apply {
            customName(bossName)
            isCustomNameVisible = true
            health = bossMaxHealth
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = bossMaxHealth
            addPotionEffect(PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 3, false, false))
        }

    override fun despawn(entity: LivingEntity) {
        entity.remove()
    }

    override fun onDeath(entity: LivingEntity) {
        // Leather, saddle drops
    }

    override fun onDamage(
        entity: LivingEntity,
        damage: Double,
    ) {
        // Charge attack that knocks back
    }

    override fun onTick(entity: LivingEntity) {
        // Rapid movement, trampling attack
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Increased speed when damaged
    }
}
