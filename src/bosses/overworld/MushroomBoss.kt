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
 * A fungal entity that spreads spores across the mushroom island.
 */
internal object MushroomBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<bold><gradient:#FF69B4:#DC143C>Mycelia, the Spore Queen</gradient></bold>")
    override val entityType: EntityType = EntityType.MOOSHROOM
    override val bossMaxHealth: Double = 250.0
    override val drops: List<ItemStack> get() = emptyList()

    override val bossBar: BossBar =
        BossBar.bossBar(
            bossName,
            1.0f,
            BossBar.Color.PINK,
            BossBar.Overlay.PROGRESS,
        )

    override fun spawn(location: Location): LivingEntity =
        (location.world.spawnEntity(location, entityType) as LivingEntity).apply {
            customName(bossName)
            isCustomNameVisible = true
            health = bossMaxHealth
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = bossMaxHealth
            addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, Int.MAX_VALUE, 1, false, false))
        }

    override fun despawn(entity: LivingEntity) {
        entity.remove()
    }

    override fun onDeath(entity: LivingEntity) {
        // Mushroom soup, red/brown mushrooms, mooshroom spawn egg
    }

    override fun onDamage(
        entity: LivingEntity,
        damage: Double,
    ) {
        // Releases confusing spores (nausea effect)
    }

    override fun onTick(entity: LivingEntity) {
        // Spreads mycelium, spawns mini mooshrooms
    }

    @EventHandler
    fun on(event: EntityDamageEvent) {
        // Heals when on mycelium
    }
}
