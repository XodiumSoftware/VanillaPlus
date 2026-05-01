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
 * A mighty guardian of the cold taiga forests.
 */
internal object TaigaBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#00CED1:#4682B4>Bjorn, the Frost Sentinel</gradient></bold>")
    override val entityType: EntityType = EntityType.POLAR_BEAR
    override val bossMaxHealth: Double = 300.0
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
            addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, Int.MAX_VALUE, 0, false, false))
            addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, Int.MAX_VALUE, 1, false, false))
        }

    override fun despawn(entity: LivingEntity) {
        entity.remove()
    }

    override fun onDeath(entity: LivingEntity) {
        // Frost-themed drops
    }

    override fun onDamage(
        entity: LivingEntity,
        damage: Double,
    ) {
        // Chance to freeze attacker
    }

    override fun onTick(entity: LivingEntity) {
        // Leaves frosted trail, slow nearby enemies
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Immune to freezing damage
    }
}
