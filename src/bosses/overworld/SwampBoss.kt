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
 * A grotesque creature that lurks in the murky swamp waters.
 */
internal object SwampBoss : BossInterface {
    override val name: String = "Morgath, the Bog Wraith"
    override val entityType: EntityType = EntityType.WITCH
    override val bossMaxHealth: Double = 160.0
    override val drops: List<ItemStack> get() = emptyList()

    override val bossBar: BossBar =
        BossBar.bossBar(
            MM.deserialize("<bold><gradient:#556B2F:#8B4513>$name</gradient></bold>"),
            1.0f,
            BossBar.Color.GREEN,
            BossBar.Overlay.PROGRESS,
        )

    override fun spawn(location: Location): LivingEntity =
        (location.world.spawnEntity(location, entityType) as LivingEntity).apply {
            customName(MM.deserialize("<bold><gradient:#556B2F:#8B4513>$name</gradient></bold>"))
            isCustomNameVisible = true
            health = bossMaxHealth
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = bossMaxHealth
            addPotionEffect(PotionEffect(PotionEffectType.WATER_BREATHING, Int.MAX_VALUE, 0, false, false))
        }

    override fun despawn(entity: LivingEntity) {
        entity.remove()
    }

    override fun onDeath(entity: LivingEntity) {
        // Alchemy ingredients and potions
    }

    override fun onDamage(
        entity: LivingEntity,
        damage: Double,
    ) {
        // Throws harmful potions
    }

    override fun onTick(entity: LivingEntity) {
        // Spawn slimes, apply poison to nearby players
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Heals when in water
    }
}
