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
 * A pollen spirit that thrives among the flowers.
 */
internal object FlowerForestBoss : BossInterface {
    override val name: String = "Flora, the Pollen Sprite"
    override val entityType: EntityType = EntityType.BEE
    override val bossMaxHealth: Double = 80.0
    override val drops: List<ItemStack> get() = emptyList()

    override val bossBar: BossBar =
        BossBar.bossBar(
            MM.deserialize("<bold><gradient:#FFB6C1:#FFD700:#FFF0F5>$name</gradient></bold>"),
            1.0f,
            BossBar.Color.PINK,
            BossBar.Overlay.PROGRESS,
        )

    override fun spawn(location: Location): LivingEntity =
        (location.world.spawnEntity(location, entityType) as LivingEntity).apply {
            customName(MM.deserialize("<bold><gradient:#FFB6C1:#FFD700:#FFF0F5>$name</gradient></bold>"))
            isCustomNameVisible = true
            health = bossMaxHealth
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = bossMaxHealth
            addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, Int.MAX_VALUE, 2, false, false))
        }

    override fun despawn(entity: LivingEntity) {
        entity.remove()
    }

    override fun onDeath(entity: LivingEntity) {
        // Honeycomb, flower drops
    }

    override fun onDamage(
        entity: LivingEntity,
        damage: Double,
    ) {
        // Summon angry bee swarm
    }

    override fun onTick(entity: LivingEntity) {
        // Pollen cloud (blindness effect), rapid regeneration near flowers
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Split into smaller bees on death
    }
}
