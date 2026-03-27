package org.xodium.vanillaplus.mobs

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Horse
import org.bukkit.entity.Skeleton
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.MobInterface
import org.xodium.vanillaplus.utils.Utils.MM
import kotlin.random.Random

/** A netherite-clad skeleton mounted on a black horse. */
internal object DarkKnight : MobInterface<Skeleton, Horse> {
    override val mobClass = Skeleton::class.java
    override val mountClass = Horse::class.java

    override fun mob(entity: Skeleton) {
        entity.apply {
            customName(MM.deserialize("<b><color:#7B2FBE>Dark Knight</color></b>"))
            isCustomNameVisible = true
            isPersistent = true
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = 50.0
            health = 50.0
            equipment.apply {
                helmet = ItemStack.of(Material.NETHERITE_HELMET)
                chestplate = ItemStack.of(Material.NETHERITE_CHESTPLATE)
                leggings = ItemStack.of(Material.NETHERITE_LEGGINGS)
                boots = ItemStack.of(Material.NETHERITE_BOOTS)
                setItemInMainHand(
                    ItemStack.of(if (Random.nextBoolean()) Material.NETHERITE_SWORD else Material.NETHERITE_SPEAR),
                )
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

    override fun mount(entity: Horse) {
        entity.apply {
            color = Horse.Color.BLACK
            isTamed = true
            isPersistent = true
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = 25.0
            health = 25.0
            inventory.armor = ItemStack.of(Material.NETHERITE_HORSE_ARMOR)
        }
    }
}
