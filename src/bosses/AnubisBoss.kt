package org.xodium.illyriaplus.bosses

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.interfaces.BossInterface

/**
 * A pharaoh-like boss that rules the scorching desert sands.
 */
internal object AnubisBoss : BossInterface {
    override val bossName: Component =
        MM.deserialize("<b><gradient:#FF8C00:#FFD700>Anubis</gradient></b>")
    override val bossType: EntityType = EntityType.HUSK
    override val bossBar: BossBar =
        BossBar.bossBar(
            bossName,
            1.0f,
            BossBar.Color.YELLOW,
            BossBar.Overlay.PROGRESS,
            setOf(BossBar.Flag.PLAY_BOSS_MUSIC, BossBar.Flag.CREATE_WORLD_FOG),
        )
    override val drops: List<ItemStack> get() = emptyList()
    override val attributes: Map<Attribute, Double> =
        mapOf(
            Attribute.MAX_HEALTH to 250.0,
            Attribute.ATTACK_DAMAGE to 10.0,
            Attribute.KNOCKBACK_RESISTANCE to 0.5,
            Attribute.SCALE to 1.8,
        )
    override val equipment: Map<EquipmentSlot, ItemStack> =
        mapOf(
            EquipmentSlot.HAND to ItemStack.of(Material.NETHERITE_SWORD),
            EquipmentSlot.OFF_HAND to ItemStack.of(Material.LEAD),
            EquipmentSlot.HEAD to ItemStack.of(Material.NETHERITE_HELMET),
            EquipmentSlot.CHEST to ItemStack.of(Material.NETHERITE_CHESTPLATE),
            EquipmentSlot.LEGS to ItemStack.of(Material.NETHERITE_LEGGINGS),
            EquipmentSlot.FEET to ItemStack.of(Material.NETHERITE_BOOTS),
        )

    override fun ability(entity: LivingEntity) {
        val target = entity.world.getNearbyPlayers(entity.location, 20.0).randomOrNull() ?: return
        val direction =
            entity.location
                .toVector()
                .subtract(target.location.toVector())
                .normalize()
        val distance = target.location.distance(entity.location)
        val steps = (distance * 2).toInt().coerceAtLeast(5)

        for (i in 0..steps) {
            val progress = i / steps.toDouble()
            val point = target.location.clone().add(direction.clone().multiply(distance * progress))

            entity.world.spawnParticle(Particle.WITCH, point, 2, 0.1, 0.1, 0.1, 0.0)
        }

        val pullDirection = direction.clone().multiply(0.8)

        target.velocity = target.velocity.add(pullDirection)
        target.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 40, 2))
    }
}
