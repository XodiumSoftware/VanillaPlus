package org.xodium.vanillaplus.modules

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.BannerPatternLayers
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Horse
import org.bukkit.entity.Skeleton
import org.bukkit.entity.Zombie
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.Utils.MM
import kotlin.random.Random

/** Represents a module handling horde mechanics within the system. */
internal object HordeModule : ModuleInterface {
    /** Applies a black base with a red skull pattern to this [ItemStack] shield. */
    @Suppress("UnstableApiUsage")
    private val shield =
        ItemStack.of(Material.SHIELD).apply {
            setData(DataComponentTypes.BASE_COLOR, DyeColor.BLACK)
            setData(
                DataComponentTypes.BANNER_PATTERNS,
                BannerPatternLayers
                    .bannerPatternLayers()
                    .add(Pattern(DyeColor.RED, PatternType.SKULL))
                    .build(),
            )
        }

    /** Configures this [Skeleton] as a Dark Knight, applying its name, persistence, netherite loadout, and 50 health. */
    private fun Skeleton.darkKnight() {
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

    /** Configures this [Horse] as the Dark Knight's mount — black, tamed, persistent, and armored with netherite. */
    private fun Horse.darkKnightHorse() {
        color = Horse.Color.BLACK
        isTamed = true
        isPersistent = true
        getAttribute(Attribute.MAX_HEALTH)?.baseValue = 25.0
        health = 25.0
        inventory.armor = ItemStack.of(Material.NETHERITE_HORSE_ARMOR)
    }

    /** Configures this [Zombie] as a Goblin, applying its name, persistence, full iron armor, and a random ranged weapon. */
    private fun Zombie.goblin() {
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

    /** Configures this [Zombie] as an Orc, applying its name, persistence, and full iron loadout. */
    private fun Zombie.orc() {
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

    /** Configures this [Zombie] as a Troll, applying its name, persistence, full chainmail armor, and a mace. */
    private fun Zombie.troll() {
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

    /** Configures this [Zombie] as a Warlord, applying its name, persistence, netherite loadout with a carved pumpkin helmet, mace, and shield. */
    @Suppress("UnstableApiUsage")
    private fun Zombie.warlord() {
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

    /** Configures this [Horse] as the Warlord's mount — white, tamed, persistent, and armored with netherite. */
    private fun Horse.warlordHorse() {
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
