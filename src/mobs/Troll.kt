package org.xodium.vanillaplus.mobs

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Entity
import org.bukkit.entity.Zombie
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.MobInterface
import org.xodium.vanillaplus.utils.Utils.MM

/** A massive zombie brawler with chainmail armor and a mace. */
internal object Troll : MobInterface<Zombie, Entity> {
    override val mobClass = Zombie::class.java

    override fun mob(entity: Zombie) {
        entity.apply {
            customName(MM.deserialize("<b><color:#7A8B6F>Troll</color></b>"))
            isCustomNameVisible = true
            isPersistent = true
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = 100.0
            getAttribute(Attribute.SCALE)?.baseValue = 3.0
            health = 100.0
            equipment.apply {
                helmet = ItemStack.of(Material.CHAINMAIL_HELMET)
                chestplate = ItemStack.of(Material.CHAINMAIL_CHESTPLATE)
                leggings = ItemStack.of(Material.CHAINMAIL_LEGGINGS)
                boots = ItemStack.of(Material.CHAINMAIL_BOOTS)
                setItemInMainHand(ItemStack.of(Material.MACE))
                helmetDropChance = 0f
                chestplateDropChance = 0f
                leggingsDropChance = 0f
                bootsDropChance = 0f
                itemInMainHandDropChance = 0f
            }
        }
    }
}
