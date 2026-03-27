package org.xodium.vanillaplus.mobs

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Entity
import org.bukkit.entity.Zombie
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.MobInterface
import org.xodium.vanillaplus.utils.Utils.MM

/** A large, heavily armored zombie spearman with a shield. */
internal object Orc : MobInterface<Zombie, Entity> {
    override val mobClass = Zombie::class.java

    override fun mob(entity: Zombie) {
        entity.apply {
            customName(MM.deserialize("<b><color:#B87333>Orc</color></b>"))
            isCustomNameVisible = true
            isPersistent = true
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = 40.0
            getAttribute(Attribute.SCALE)?.baseValue = 1.5
            health = 40.0
            equipment.apply {
                helmet = ItemStack.of(Material.IRON_HELMET)
                chestplate = ItemStack.of(Material.IRON_CHESTPLATE)
                leggings = ItemStack.of(Material.IRON_LEGGINGS)
                boots = ItemStack.of(Material.IRON_BOOTS)
                setItemInMainHand(ItemStack.of(Material.IRON_SPEAR))
                setItemInOffHand(shield)
                helmetDropChance = 0f
                chestplateDropChance = 0f
                leggingsDropChance = 0f
                bootsDropChance = 0f
                itemInMainHandDropChance = 0f
                itemInOffHandDropChance = 0f
            }
        }
    }
}
