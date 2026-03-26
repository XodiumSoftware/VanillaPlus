package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.BannerPatternLayers
import org.bukkit.DyeColor
import org.bukkit.Location
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
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import org.xodium.vanillaplus.utils.Utils.MM
import kotlin.random.Random

/** Represents a module handling horde mechanics within the system. */
internal object HordeModule : ModuleInterface {
    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("horde")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .playerExecuted { player, _ -> spawnFormation(player.location) },
                "Spawns a horde formation at your location",
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.horde".lowercase(),
                "Allows spawning a horde formation",
                PermissionDefault.OP,
            ),
        )

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

    /**
     * Spawns a full horde formation centered at the given [location].
     * The formation follows a medieval rows layout: goblins up front, orcs behind them,
     * trolls further back, dark knights flanking, and the warlord commanding from the rear.
     * @param location The [Location] used as the front-center of the formation.
     */
    fun spawnFormation(location: Location) {
        spawnRow(location, rowZ = 0.0, spacing = 4.0, count = 6) { location ->
            location.world.spawn(location, Zombie::class.java) { it.goblin() }
        }
        spawnRow(location, rowZ = 8.0, spacing = 5.0, count = 4) { location ->
            location.world.spawn(location, Zombie::class.java) { it.orc() }
        }
        spawnRow(location, rowZ = 18.0, spacing = 10.0, count = 2) { location ->
            location.world.spawn(location, Zombie::class.java) { it.troll() }
        }
        spawnRow(location, rowZ = 28.0, spacing = 8.0, count = 2) { spawnDarkKnight(it) }
        spawnWarlord(location.clone().add(0.0, 0.0, 36.0))
    }

    /**
     * Spawns a Dark Knight at the given location — a [Skeleton] riding a black [Horse].
     * @param location The [Location] at which to spawn the Dark Knight.
     * @return The spawned [Skeleton] entity.
     */
    private fun spawnDarkKnight(location: Location): Skeleton {
        val horse = location.world.spawn(location, Horse::class.java) { it.darkKnightHorse() }
        return location.world.spawn(location, Skeleton::class.java) { it.darkKnight() }.also { horse.addPassenger(it) }
    }

    /**
     * Spawns a Warlord at the given location — a [Zombie] riding a white [Horse].
     * @param location The [Location] at which to spawn the Warlord.
     * @return The spawned [Zombie] entity.
     */
    private fun spawnWarlord(location: Location): Zombie {
        val horse = location.world.spawn(location, Horse::class.java) { it.warlordHorse() }
        return location.world.spawn(location, Zombie::class.java) { it.warlord() }.also { horse.addPassenger(it) }
    }

    /**
     * Spawns a centered row of entities along the X axis relative to [location].
     * @param location The origin of the formation.
     * @param rowZ The Z offset from the origin for this row.
     * @param spacing The distance between each entity in the row.
     * @param count The number of entities to spawn.
     * @param spawner A lambda that spawns a single entity at the given [Location].
     */
    private fun spawnRow(
        location: Location,
        rowZ: Double,
        spacing: Double,
        count: Int,
        spawner: (Location) -> Unit,
    ) {
        val halfWidth = (count - 1) * spacing / 2.0
        repeat(count) { i -> spawner(location.clone().add(-halfWidth + i * spacing, 0.0, rowZ)) }
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
