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
 * A pharaoh-like boss that rules the scorching desert sands.
 */
internal object DesertBoss : BossInterface {
    override val name: String = "Anubis, the Sand Pharaoh"
    override val entityType: EntityType = EntityType.HUSK
    override val bossMaxHealth: Double = 250.0
    override val drops: List<ItemStack> get() = emptyList()

    override val bossBar: BossBar =
        BossBar.bossBar(
            MM.deserialize("<bold><gradient:#FFD700:#FF8C00>$name</gradient></bold>"),
            1.0f,
            BossBar.Color.YELLOW,
            BossBar.Overlay.PROGRESS,
        )

    override fun spawn(location: Location): LivingEntity =
        (location.world.spawnEntity(location, entityType) as LivingEntity).apply {
            customName(MM.deserialize("<bold><gradient:#FFD700:#FF8C00>$name</gradient></bold>"))
            isCustomNameVisible = true
            health = bossMaxHealth
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = bossMaxHealth
            addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, Int.MAX_VALUE, 0, false, false))
        }

    override fun despawn(entity: LivingEntity) {
        entity.remove()
    }

    override fun onDeath(entity: LivingEntity) {
        // Ancient treasure drops
    }

    override fun onDamage(
        entity: LivingEntity,
        damage: Double,
    ) {
        // Chance to summon sandstorm or husks
    }

    override fun onTick(entity: LivingEntity) {
        // Apply hunger effect to nearby players
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Immune to fire damage
    }
}
