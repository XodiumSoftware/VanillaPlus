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
 * An ice-themed boss that brings winter's fury.
 */
internal object SnowBoss : BossInterface {
    override val name: String = "Aurora, the Frostbinder"
    override val entityType: EntityType = EntityType.STRAY
    override val bossMaxHealth: Double = 200.0
    override val drops: List<ItemStack> get() = emptyList()

    override val bossBar: BossBar =
        BossBar.bossBar(
            MM.deserialize("<bold><gradient:#E0FFFF:#00BFFF>$name</gradient></bold>"),
            1.0f,
            BossBar.Color.BLUE,
            BossBar.Overlay.PROGRESS,
        )

    override fun spawn(location: Location): LivingEntity =
        (location.world.spawnEntity(location, entityType) as LivingEntity).apply {
            customName(MM.deserialize("<bold><gradient:#E0FFFF:#00BFFF>$name</gradient></bold>"))
            isCustomNameVisible = true
            health = bossMaxHealth
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = bossMaxHealth
            addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, Int.MAX_VALUE, 0, false, false))
        }

    override fun despawn(entity: LivingEntity) {
        entity.remove()
    }

    override fun onDeath(entity: LivingEntity) {
        // Ice-related drops, tipped arrows
    }

    override fun onDamage(
        entity: LivingEntity,
        damage: Double,
    ) {
        // Slowness arrows with freezing effect
    }

    override fun onTick(entity: LivingEntity) {
        // Snow particle aura, freeze nearby water
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Immune to freezing, heals in powder snow
    }
}
