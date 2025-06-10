/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.managers

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import dev.triumphteam.gui.paper.kotlin.builder.chestContainer
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.title.Title
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.data.MobAttributeData
import org.xodium.vanillaplus.data.MobEquipmentData
import org.xodium.vanillaplus.utils.ExtUtils.clickSuggestCmd
import org.xodium.vanillaplus.utils.ExtUtils.il
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.FmtUtils.skylineFmt
import org.xodium.vanillaplus.utils.Utils
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.time.Duration.Companion.seconds

/** Represents the config manager within the system. */
object ConfigManager {
    private val configPath = VanillaPlus.Companion.instance.dataFolder.toPath().resolve("config.json")
    private val objectMapper = jacksonObjectMapper().apply { registerModule(JavaTimeModule()) }
    var data: ConfigData = ConfigData()

    /** Initializes the configuration by loading module states from the config file. */
    fun load() {
        if (Files.exists(configPath)) {
            VanillaPlus.Companion.instance.logger.info("Config: Loading module states.")
            data = objectMapper.readValue(Files.readString(configPath))
            VanillaPlus.Companion.instance.logger.info("Config: Module states loaded successfully.")
        } else {
            VanillaPlus.Companion.instance.logger.info("Config: No config file found, creating default states.")
            save()
        }
    }

    /** Saves the current module states to the config file. */
    private fun save() {
        VanillaPlus.Companion.instance.logger.info("Config: Saving module states.")
        Files.createDirectories(configPath.parent)
        Files.writeString(
            configPath,
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
        VanillaPlus.Companion.instance.logger.info("Config: Module states saved successfully.")
    }

    /**
     * Creates the command for the configuration GUI.
     * @return A LiteralArgumentBuilder for the "config" command.
     */
    @Suppress("UnstableApiUsage")
    fun cmd(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("config")
            .requires { it.sender.hasPermission(Perms.Config.USE) }
            .executes { it -> Utils.tryCatch(it) { gui().open(it.sender as Player) } }
    }

    /**
     * Creates a GUI for the configuration settings.
     * @return A Gui object representing the configuration GUI.
     */
    private fun gui(): Gui {
        //TODO: make a main page with all the modules inside.
        //TODO: each module has its own page with settings.
        //TODO: add handling of settings that are not boolean (e.g. strings, numbers, etc.).
        val modules = ConfigManager::class.nestedClasses.mapNotNull { kClass ->
            val enabledProp = kClass.declaredMemberProperties.find { it.name == "ENABLED" }
            if (enabledProp != null) kClass to enabledProp else null
        }
        val dynamicRows = modules.size.let { ((it - 1) / 9 + 1).coerceIn(1, 6) }
        return buildGui {
            containerType = chestContainer { rows = dynamicRows }
            spamPreventionDuration = 1.seconds
            title("<b>Config</b>".fireFmt().mm())
            statelessComponent { inv ->
                modules.forEachIndexed { idx, (kClass, enabledProp) ->
                    enabledProp.isAccessible = true
                    val obj = kClass.objectInstance ?: return@forEachIndexed
                    val enabled = enabledProp.getter.call(obj) as? Boolean ?: false
                    val mat = if (enabled) Material.GREEN_WOOL else Material.RED_WOOL
                    val name = kClass.simpleName ?: "Unknown"
                    val lore = listOf(
                        if (enabled) "<green>Enabled<reset>" else "<red>Disabled<reset>",
                        "*Requires server restart*"
                    )
                    inv[idx] = ItemBuilder.from(guiItem(mat, name.mangoFmt(), lore))
                        .asGuiItem { player, _ ->
                            val mutableProp = enabledProp as? KMutableProperty1<*, *>
                            mutableProp?.setter?.call(obj, !enabled)
                            save()
                            gui().open(player)
                        }
                }
            }
        }
    }

    /**
     * Creates a GUI item.
     * @param material The material of the item.
     * @param title The title of the item.
     * @param lore The lore of the item.
     * @return An ItemStack representing the GUI item.
     */
    @Suppress("UnstableApiUsage")
    private fun guiItem(material: Material, title: String, lore: List<String>): ItemStack {
        return ItemStack.of(material).apply {
            setData(DataComponentTypes.CUSTOM_NAME, title.mm())
            setData(DataComponentTypes.LORE, lore.il())
        }
    }

    /** Configuration settings for the AutoRestartModule. */
    object AutoRestartModule {
        /** The name of the boss bar, formatted with the display time. */
        private var BOSSBAR_NAME: String = "⚡ RESTARTING in <time> minute(s) ⚡".fireFmt()

        /** Bossbar for the auto-restart. */
        var BOSSBAR: BossBar = BossBar.bossBar(
            BOSSBAR_NAME.mm(),
            1.0f,
            BossBar.Color.RED,
            BossBar.Overlay.PROGRESS
        )
    }

    /** Configuration settings for the BooksModule. */
    object BooksModule {
        //TODO: suggest cmd not working because of minecraft bug.
        /** The Guide book. */
        var GUIDE_BOOK: Book = Book.book(
            "Guide".fireFmt().mm(),
            VanillaPlus.Companion.instance::class.simpleName.toString().fireFmt().mm(),
            listOf(
                // Page 1
                """
                <b><u>${"Tips & Tricks".fireFmt()}
                
                <gold>▶ ${"/home".clickSuggestCmd(Utils.cmdHover).skylineFmt()}
                <dark_gray>Teleport to your home
                
                <gold>▶ ${"/skills".clickSuggestCmd(Utils.cmdHover).skylineFmt()}
                <dark_gray>Opens up the Skills GUI
                
                <gold>▶ ${"/rtp".clickSuggestCmd(Utils.cmdHover).skylineFmt()}
                <dark_gray>Random teleport in the current dimension
                """.trimIndent(),

                // Page 2
                """
                <gold>▶ ${"/unload".clickSuggestCmd(Utils.cmdHover).skylineFmt()}
                <dark_gray>Unloads your inventory into nearby chests
                
                <gold>▶ ${"/search".clickSuggestCmd(Utils.cmdHover).skylineFmt()}
                <dark_gray>Search into nearby chests for an item
                
                <gold>▶ ${"/tpa [player]".clickSuggestCmd().skylineFmt()}
                <dark_gray>Request to teleport to a player
                """.trimIndent(),

                // Page 3
                """
                <gold>▶ ${"/condense".clickSuggestCmd(Utils.cmdHover).skylineFmt()}
                <dark_gray>Condenses resources (if possible) to their highest form (blocks)
                
                <gold>▶ ${"/uncondense".clickSuggestCmd(Utils.cmdHover).skylineFmt()}
                <dark_gray>Uncondenses resources (if possible) to their lowest form (items)
                """.trimIndent(),

                // Page 4
                """
                <gold>▶ ${"Enchantment max level".skylineFmt()}
                <dark_gray>has been incremented by <red><b>x2<reset>
                
                <gold>▶ ${"During an Eclipse".skylineFmt()}
                <dark_gray>A horde will spawn where the mobs are stronger than usual
                """.trimIndent()
            ).mm()
        )

        /** The Rules book. */
        var RULES_BOOK: Book = Book.book(
            "Rules".fireFmt().mm(),
            VanillaPlus.Companion.instance::class.simpleName.toString().fireFmt().mm(),
            listOf(
                // Page 1: Player Rules (1-7)
                """
                <b><u><dark_aqua>Player Rules:<reset>
        
                <gold>▶ <dark_aqua>01 <dark_gray>| <red>No Griefing
                <gold>▶ <dark_aqua>02 <dark_gray>| <red>No Spamming
                <gold>▶ <dark_aqua>03 <dark_gray>| <red>No Advertising
                <gold>▶ <dark_aqua>04 <dark_gray>| <red>No Cursing/No Constant Cursing
                <gold>▶ <dark_aqua>05 <dark_gray>| <red>No Trolling/Flaming
                <gold>▶ <dark_aqua>06 <dark_gray>| <red>No Asking for OP, Ranks, or Items
                <gold>▶ <dark_aqua>07 <dark_gray>| <red>Respect all Players
                """.trimIndent(),

                // Page 2: Player Rules (8-13)
                """
                <gold>▶ <dark_aqua>08 <dark_gray>| <red>Obey Staff they are the Law Enforcers
                <gold>▶ <dark_aqua>09 <dark_gray>| <red>No Racist or Sexist Remarks
                <gold>▶ <dark_aqua>10 <dark_gray>| <red>No Mods/Hacks
                <gold>▶ <dark_aqua>11 <dark_gray>| <red>No Full Caps Messages
                <gold>▶ <dark_aqua>12 <dark_gray>| <red>No 1x1 Towers
                <gold>▶ <dark_aqua>13 <dark_gray>| <red>Build in (Fantasy)Medieval style
                """.trimIndent(),

                // Page 3: Mod/Admin Rules
                """
                <b><u><dark_aqua>Mod/Admin Rules:<reset>
        
                <gold>▶ <dark_aqua>01 <dark_gray>| <red>Be Responsible with the power you are given as staff
                <gold>▶ <dark_aqua>02 <dark_gray>| <red>Do not spawn blocks or items for other players
                <gold>▶ <dark_aqua>03 <dark_gray>| <red>When Trading, only buy and sell legit items
                <gold>▶ <dark_aqua>05 <dark_gray>| <red>No Power Abuse
                """.trimIndent()
            ).mm()
        )
    }

    /** Configuration settings for the DoorsModule. */
    object DoorsModule {
        /** The sound effect used for closing doors. */
        var SOUND_DOOR_CLOSE: Sound = Sound.sound(
            org.bukkit.Sound.BLOCK_IRON_DOOR_CLOSE,
            Sound.Source.BLOCK,
            1.0f,
            1.0f
        )

        /** The sound effect used for closing gates. */
        var SOUND_GATE_CLOSE: Sound = Sound.sound(
            org.bukkit.Sound.BLOCK_FENCE_GATE_CLOSE,
            Sound.Source.BLOCK,
            1.0f,
            1.0f
        )

        /** The sound effect used for knocking. */
        var SOUND_KNOCK: Sound = Sound.sound(
            org.bukkit.Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
            Sound.Source.HOSTILE,
            1.0f,
            1.0f
        )
    }

    /** Configuration settings for the eclipseModule. */
    object EclipseModule {
        /** The list of attributes for mobs during an eclipse. */
        var MOB_ATTRIBUTE: List<MobAttributeData> = listOf(
            MobAttributeData(
                EntityType.entries,
                mapOf(
                    Attribute.ATTACK_DAMAGE to { it * 2.0 },
                    Attribute.MAX_HEALTH to { it * 2.0 },
                    Attribute.FOLLOW_RANGE to { it * 2.0 },
                    Attribute.MOVEMENT_EFFICIENCY to { it * 2.0 },
                    Attribute.WATER_MOVEMENT_EFFICIENCY to { it * 2.0 },
                    Attribute.SPAWN_REINFORCEMENTS to { it * 2.0 },
                ),
                10.0
            ),
            MobAttributeData(
                listOf(EntityType.SPIDER),
                mapOf(
                    Attribute.SCALE to { it * 4.0 },
                ),
                1.5
            )
        )

        /** The list of equipment for mobs during an eclipse. */
        var MOB_EQUIPMENT: List<MobEquipmentData> = listOf(
            MobEquipmentData(EquipmentSlot.HEAD, ItemStack(Material.NETHERITE_HELMET), 0.0f),
            MobEquipmentData(EquipmentSlot.CHEST, ItemStack(Material.NETHERITE_CHESTPLATE), 0.0f),
            MobEquipmentData(EquipmentSlot.LEGS, ItemStack(Material.NETHERITE_LEGGINGS), 0.0f),
            MobEquipmentData(EquipmentSlot.FEET, ItemStack(Material.NETHERITE_BOOTS), 0.0f),
            MobEquipmentData(
                EquipmentSlot.HAND,
                ItemStack(
                    listOf(
                        Material.NETHERITE_SWORD,
                        Material.NETHERITE_AXE,
                        Material.BOW
                    ).random()
                ),
                0.0f
            ),
            MobEquipmentData(EquipmentSlot.OFF_HAND, ItemStack(Material.SHIELD), 0.0f)
        )

//        /** The list of mobs that are excluded from the eclipse buff. */
//        var EXCLUDED_MOBS: Set<EntityType> = setOf(EntityType.ENDERMAN)

        /** The title message displayed when the eclipse is active. */
        var ECLIPSE_START_TITLE: Title =
            Title.title("An Eclipse is rising!".fireFmt().mm(), "Stay inside ;)".mangoFmt().mm())

        /** The title message displayed when the eclipse is inactive. */
        var ECLIPSE_END_TITLE: Title =
            Title.title("An Eclipse is setting!".fireFmt().mm(), "You can go outside now :P".mangoFmt().mm())

        /** The sound effect used for when the eclipse is active. */
        var ECLIPSE_START_SOUND: Sound = Sound.sound(
            org.bukkit.Sound.ENTITY_WITHER_SPAWN,
            Sound.Source.HOSTILE,
            1.0f,
            1.0f
        )

        /** The sound effect used for when the eclipse is inactive. */
        var ECLIPSE_END_SOUND: Sound = Sound.sound(
            org.bukkit.Sound.ENTITY_WITHER_DEATH,
            Sound.Source.HOSTILE,
            1.0f,
            1.0f
        )

//        /** The initial delay before the first eclipse. */
//        var INIT_DELAY: Long = TimeUtils.seconds(0)
//
//        /** The interval between eclipses. */
//        var INTERVAL: Long = TimeUtils.seconds(10)
    }

    /** Configuration settings for the InvUnloadModule. */
    object InvUnloadModule {
        /** The sound effect used for unloading. */
        var SOUND_ON_UNLOAD: Sound = Sound.sound(
            org.bukkit.Sound.ENTITY_PLAYER_LEVELUP,
            Sound.Source.PLAYER,
            1.0f,
            1.0f
        )
    }
}