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
 * A leviathan that rules the ocean depths.
 */
internal object OceanBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#1E90FF:#00008B>Leviathan, the Abyssal Tyrant</gradient></bold>")
    override val entityType: EntityType = EntityType.GUARDIAN
    override val bossMaxHealth: Double = 400.0
    override val drops: List<ItemStack> get() = emptyList()

    override val bossBar: BossBar =
        BossBar.bossBar(
            bossName,
            1.0f,
            BossBar.Color.BLUE,
            BossBar.Overlay.PROGRESS,
        )

    override fun spawn(location: Location): LivingEntity =
        (location.world.spawnEntity(location, entityType) as LivingEntity).apply {
            customName(bossName)
            isCustomNameVisible = true
            health = bossMaxHealth
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = bossMaxHealth
            addPotionEffect(PotionEffect(PotionEffectType.WATER_BREATHING, Int.MAX_VALUE, 0, false, false))
        }

    override fun despawn(entity: LivingEntity) {
        entity.remove()
    }

    override fun onDeath(entity: LivingEntity) {
        // Prismarine, sponges, and ocean treasures
    }

    override fun onDamage(
        entity: LivingEntity,
        damage: Double,
    ) {
        // Fires guardian beam
    }

    override fun onTick(entity: LivingEntity) {
        // Mining fatigue aura, summon drowned
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Reduced damage when in water
    }
}
