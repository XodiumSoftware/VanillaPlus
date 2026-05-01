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
 * A graceful archer that guards the birch groves.
 */
internal object BirchForestBoss : BossInterface {
    override val name: String = "Sylvara, the White Huntress"
    override val entityType: EntityType = EntityType.SKELETON
    override val bossMaxHealth: Double = 140.0
    override val drops: List<ItemStack> get() = emptyList()

    override val bossBar: BossBar =
        BossBar.bossBar(
            MM.deserialize("<bold><gradient:#FFFAF0:#F0E68C>$name</gradient></bold>"),
            1.0f,
            BossBar.Color.WHITE,
            BossBar.Overlay.PROGRESS,
        )

    override fun spawn(location: Location): LivingEntity =
        (location.world.spawnEntity(location, entityType) as LivingEntity).apply {
            customName(MM.deserialize("<bold><gradient:#FFFAF0:#F0E68C>$name</gradient></bold>"))
            isCustomNameVisible = true
            health = bossMaxHealth
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = bossMaxHealth
            addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, 0, false, false))
        }

    override fun despawn(entity: LivingEntity) {
        entity.remove()
    }

    override fun onDeath(entity: LivingEntity) {
        // Birch wood, bow drops
    }

    override fun onDamage(
        entity: LivingEntity,
        damage: Double,
    ) {
        // Swift dodge and arrow barrage
    }

    override fun onTick(entity: LivingEntity) {
        // Cloaked in birch leaves, rapid fire
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Camouflage in birch forests
    }
}
