package org.xodium.vanillaplus.mobs

import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Horse
import org.bukkit.entity.Zombie
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.MobInterface
import org.xodium.vanillaplus.utils.Utils.MM

/** A netherite warlord zombie mounted on a white horse — the formation commander. */
@Suppress("UnstableApiUsage")
internal object Warlord : MobInterface<Zombie, Horse> {
    override fun mob(entity: Zombie) {
        entity.apply {
            customName(MM.deserialize("<b><color:#B22222>Warlord</color></b>"))
            isCustomNameVisible = true
            isPersistent = true
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = 300.0
            getAttribute(Attribute.SCALE)?.baseValue = 1.25
            health = 150.0
            equipment.apply {
                helmet =
                    ItemStack.of(Material.CARVED_PUMPKIN).apply {
                        setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
                    }
                chestplate =
                    ItemStack.of(Material.NETHERITE_CHESTPLATE).apply {
                        addEnchantment(Enchantment.PROTECTION, 3)
                        addEnchantment(Enchantment.THORNS, 2)
                    }
                leggings =
                    ItemStack.of(Material.NETHERITE_LEGGINGS).apply {
                        addEnchantment(Enchantment.PROTECTION, 3)
                    }
                boots =
                    ItemStack.of(Material.NETHERITE_BOOTS).apply {
                        addEnchantment(Enchantment.PROTECTION, 3)
                        addEnchantment(Enchantment.FEATHER_FALLING, 2)
                    }
                setItemInMainHand(
                    ItemStack.of(Material.MACE).apply {
                        addEnchantment(Enchantment.DENSITY, 4)
                        addEnchantment(Enchantment.FIRE_ASPECT, 2)
                    },
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
            color = Horse.Color.WHITE
            isTamed = true
            isPersistent = true
            getAttribute(Attribute.MAX_HEALTH)?.baseValue = 150.0
            getAttribute(Attribute.SCALE)?.baseValue = 1.25
            health = 150.0
            inventory.armor = ItemStack.of(Material.NETHERITE_HORSE_ARMOR)
            equipment.setDropChance(EquipmentSlot.BODY, 0f)
        }
    }

    override fun spawn(location: Location): Zombie =
        location.world.spawn(location, Zombie::class.java) { mob(it) }.also { rider ->
            location.world.spawn(location, Horse::class.java) { mount(it) }.addPassenger(rider)
        }
}
