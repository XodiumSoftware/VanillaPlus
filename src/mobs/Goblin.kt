package org.xodium.vanillaplus.mobs

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Entity
import org.bukkit.entity.Zombie
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.MobInterface
import org.xodium.vanillaplus.utils.Utils.MM
import kotlin.random.Random

/** A small, fast zombie archer with iron armor and a randomized ranged weapon. */
internal object Goblin : MobInterface<Zombie, Entity> {
    override fun mob(entity: Zombie) {
        entity.apply {
            customName(MM.deserialize("<b><color:#47B33B>Goblin</color></b>"))
            isCustomNameVisible = true
            isPersistent = true
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = 25.0
            getAttribute(Attribute.SCALE)?.baseValue = 0.75
            health = 25.0
            equipment.apply {
                helmet = ItemStack.of(Material.IRON_HELMET)
                chestplate = ItemStack.of(Material.IRON_CHESTPLATE)
                leggings = ItemStack.of(Material.IRON_LEGGINGS)
                boots = ItemStack.of(Material.IRON_BOOTS)
                setItemInMainHand(ItemStack.of(if (Random.nextBoolean()) Material.BOW else Material.CROSSBOW))
                helmetDropChance = 0f
                chestplateDropChance = 0f
                leggingsDropChance = 0f
                bootsDropChance = 0f
                itemInMainHandDropChance = 0f
            }
        }
    }

    override fun spawn(location: Location): Zombie = location.world.spawn(location, Zombie::class.java) { mob(it) }
}
