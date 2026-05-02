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
 * A ferocious beast that stalks the dense jungle canopy.
 */
internal object JungleBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#32CD32:#228B22>Kaa'tar, the Vine Tyrant</gradient></bold>")
    override val entityType: EntityType = EntityType.OCELOT
    override val bossMaxHealth: Double = 150.0
    override val drops: List<ItemStack> get() = emptyList()

    override val bossBar: BossBar =
        BossBar.bossBar(
            bossName,
            1.0f,
            BossBar.Color.GREEN,
            BossBar.Overlay.PROGRESS,
        )

    override fun spawn(location: Location): LivingEntity =
        (location.world.spawnEntity(location, entityType) as LivingEntity).apply {
            customName(bossName)
            isCustomNameVisible = true
            health = bossMaxHealth
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = bossMaxHealth
            addPotionEffect(PotionEffect(PotionEffectType.SPEED, Int.MAX_VALUE, 2, false, false))
        }

    override fun despawn(entity: LivingEntity) {
        entity.remove()
    }

    override fun onDeath(entity: LivingEntity) {
        // Jungle treasure drops
    }

    override fun onDamage(
        entity: LivingEntity,
        damage: Double,
    ) {
        // Leaps away and counter-attacks
    }

    override fun onTick(entity: LivingEntity) {
        // Climbs vines and leaves automatically
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Reduced fall damage
    }
}
