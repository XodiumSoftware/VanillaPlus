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
 * A stone giant that dwells in the mountain peaks.
 */
internal object MountainBoss : BossInterface {
    override val name: String = "Titan, the Peak Colossus"
    override val entityType: EntityType = EntityType.IRON_GOLEM
    override val bossMaxHealth: Double = 500.0
    override val drops: List<ItemStack> get() = emptyList()

    override val bossBar: BossBar =
        BossBar.bossBar(
            MM.deserialize("<bold><gradient:#D3D3D3:#696969>$name</gradient></bold>"),
            1.0f,
            BossBar.Color.WHITE,
            BossBar.Overlay.PROGRESS,
        )

    override fun spawn(location: Location): LivingEntity =
        (location.world.spawnEntity(location, entityType) as LivingEntity).apply {
            customName(MM.deserialize("<bold><gradient:#D3D3D3:#696969>$name</gradient></bold>"))
            isCustomNameVisible = true
            health = bossMaxHealth
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = bossMaxHealth
            addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, Int.MAX_VALUE, 2, false, false))
            addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, Int.MAX_VALUE, 1, false, false))
        }

    override fun despawn(entity: LivingEntity) {
        entity.remove()
    }

    override fun onDeath(entity: LivingEntity) {
        // Iron, stone, and emerald drops
    }

    override fun onDamage(
        entity: LivingEntity,
        damage: Double,
    ) {
        // Knockback attack, earthquake effect
    }

    override fun onTick(entity: LivingEntity) {
        // Stomp creates shockwaves, throw boulders
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Huge fall damage resistance
    }
}
