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
 * A boss that roams the plains, commanding the wind and grass.
 */
internal object PlainsBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FFD700:#FFA500>Zephyr, the Tempest Walker</gradient></bold>")
    override val entityType: EntityType = EntityType.ZOMBIE
    override val bossMaxHealth: Double = 200.0
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
            addPotionEffect(PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 1, false, false))
        }

    override fun despawn(entity: LivingEntity) {
        entity.remove()
    }

    override fun onDeath(entity: LivingEntity) {
        // Drops handled via loot table or custom logic
    }

    override fun onDamage(
        entity: LivingEntity,
        damage: Double,
    ) {
        // Chance to dodge and counter with wind gust
    }

    override fun onTick(entity: LivingEntity) {
        // Passive wind aura that pushes nearby players
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Handle damage events
    }
}
