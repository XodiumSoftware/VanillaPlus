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
        BossBar.bossBar(bossName, 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS)
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
            EquipmentSlot.HAND to ItemStack.of(Material.NETHERITE_SPEAR),
            EquipmentSlot.OFF_HAND to ItemStack.of(Material.LEAD),
            EquipmentSlot.HEAD to ItemStack.of(Material.NETHERITE_HELMET),
            EquipmentSlot.CHEST to ItemStack.of(Material.NETHERITE_CHESTPLATE),
            EquipmentSlot.LEGS to ItemStack.of(Material.NETHERITE_LEGGINGS),
            EquipmentSlot.FEET to ItemStack.of(Material.NETHERITE_BOOTS),
        )

    override fun ability(entity: LivingEntity) {
        entity.world.spawnParticle(
            Particle.FALLING_DUST,
            entity.location,
            30,
            3.0,
            2.0,
            3.0,
            0.0,
            Material.SAND.createBlockData(),
        )
        entity.world.getNearbyPlayers(entity.location, 10.0).forEach {
            it.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 60, 1))
        }
    }
}
