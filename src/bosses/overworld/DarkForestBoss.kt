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
 * A cursed entity that haunts the dark forest.
 */
internal object DarkForestBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#4B0082:#000000>Nocturne, the Shadow Lurker</gradient></bold>")
    override val entityType: EntityType = EntityType.VEX
    override val bossMaxHealth: Double = 120.0
    override val drops: List<ItemStack> get() = emptyList()

    override val bossBar: BossBar =
        BossBar.bossBar(
            bossName,
            1.0f,
            BossBar.Color.PURPLE,
            BossBar.Overlay.PROGRESS,
        )

    override fun spawn(location: Location): LivingEntity =
        (location.world.spawnEntity(location, entityType) as LivingEntity).apply {
            customName(bossName)
            isCustomNameVisible = true
            health = bossMaxHealth
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = bossMaxHealth
            addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, 0, false, false))
        }

    override fun despawn(entity: LivingEntity) {
        entity.remove()
    }

    override fun onDeath(entity: LivingEntity) {
        // Woodland mansion loot
    }

    override fun onDamage(
        entity: LivingEntity,
        damage: Double,
    ) {
        // Teleport and counter attack
    }

    override fun onTick(entity: LivingEntity) {
        // Shadow clone ability, darkness effect
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Phases through blocks
    }
}
