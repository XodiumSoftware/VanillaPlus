/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus

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
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.MobAttributeData
import org.xodium.vanillaplus.data.MobEquipmentData
import org.xodium.vanillaplus.utils.ExtUtils.clickRunCmd
import org.xodium.vanillaplus.utils.ExtUtils.clickSuggestCmd
import org.xodium.vanillaplus.utils.ExtUtils.il
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.FmtUtils.skylineFmt
import org.xodium.vanillaplus.utils.TimeUtils
import org.xodium.vanillaplus.utils.Utils
import java.time.LocalTime
import kotlin.time.Duration.Companion.seconds
import org.bukkit.Sound as BukkitSound

//TODO: add way to have the config on a gui.

/** Configuration settings. */
object Config {
    /**
     * Provides a collection of commands for the configuration settings.
     * @return A collection of LiteralArgumentBuilder objects representing the commands.
     */
    @Suppress("UnstableApiUsage")
    fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? { //TODO: register cmds.
        return listOf(
            Commands.literal("guide")
                .requires { it.sender.hasPermission(Perms.Config.USE) }
                .executes { it -> Utils.tryCatch(it) { gui().open(it.sender as Player) } })
    }

    /**
     * Creates a GUI for the configuration settings.
     * @return A Gui object representing the configuration GUI.
     */
    private fun gui(): Gui {
        return buildGui {
            containerType = chestContainer { rows = 6 } //TODO: make rows dynamic based on config.
            spamPreventionDuration = 1.seconds
            title("Config".fireFmt().mm())
            statelessComponent {
                //TODO: make the gui item setting dynamic based on the config.
                //TODO: adjust material based on config value. (e.g., if enabled, use green wool; if disabled, use red wool; if ranged value, use blue wool).
                it[1] = ItemBuilder.from(guiItem(Material.RED_WOOL, "", listOf("")))
                    .asGuiItem() //TODO: set title, lore and action based on config.
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
        /** Enables or disables the AutoRestartModule. */
        var ENABLED: Boolean = true

        /** The times of day when the server should restart. */
        var RESTART_TIMES: MutableList<LocalTime> = mutableListOf(
            LocalTime.of(0, 0),
            LocalTime.of(6, 0),
            LocalTime.of(12, 0),
            LocalTime.of(18, 0)
        )

        /** The initial delay before the first schedule. */
        var SCHEDULE_INIT_DELAY: Long = TimeUtils.seconds(0)

        /** The interval between schedule. */
        var SCHEDULE_INTERVAL: Long = TimeUtils.minutes(1)

        /** The initial delay before the first countdown. */
        var COUNTDOWN_INIT_DELAY: Long = TimeUtils.seconds(0)

        /** The interval between countdown. */
        var COUNTDOWN_INTERVAL: Long = TimeUtils.seconds(1)

        /** How many minutes before the restart to start countdown. */
        var COUNTDOWN_START_MINUTES: Int = 5

        /** The name of the boss bar, formatted with the display time. */
        var BOSSBAR_NAME: String = "⚡ RESTARTING in <time> minute(s) ⚡".fireFmt()

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
        /** Enables or disables the BookModule. */
        var ENABLED: Boolean = true

        //TODO: suggest cmd not working because of minecraft bug.
        /** The Guide book. */
        var GUIDE_BOOK: Book = Book.book(
            "Guide".fireFmt().mm(),
            instance::class.simpleName.toString().fireFmt().mm(),
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
            instance::class.simpleName.toString().fireFmt().mm(),
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

    /** Configuration settings for the DimensionsModule. */
    object DimensionsModule {
        /** Enables or disables the DimensionsModule. */
        var ENABLED: Boolean = true

        /** The radius within which to search for portals in the overworld. */
        var PORTAL_SEARCH_RADIUS: Double = 128.0
    }

    /** Configuration settings for the DiscordModule. */
    object DiscordModule {
        /** Enables or disables the DiscordModule. */
        var ENABLED: Boolean = true
    }

    /** Configuration settings for the DoorsModule. */
    object DoorsModule {
        /** Enables or disables the DoorsModule. */
        var ENABLED: Boolean = true

        /** The initial delay before the first autoclose. */
        var INIT_DELAY: Long = 1L

        /** The interval between autoclosing. */
        var INTERVAL: Long = 1L

        /** The sound effect used for closing doors. */
        var SOUND_DOOR_CLOSE: Sound = Sound.sound(
            BukkitSound.BLOCK_IRON_DOOR_CLOSE,
            Sound.Source.BLOCK,
            1.0f,
            1.0f
        )

        /** The sound effect used for closing gates. */
        var SOUND_GATE_CLOSE: Sound = Sound.sound(
            BukkitSound.BLOCK_FENCE_GATE_CLOSE,
            Sound.Source.BLOCK,
            1.0f,
            1.0f
        )

        /** The sound effect used for knocking. */
        var SOUND_KNOCK: Sound = Sound.sound(
            BukkitSound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR,
            Sound.Source.HOSTILE,
            1.0f,
            1.0f
        )

        /** Enables automatic closing of doors after a set delay. */
        var ALLOW_AUTO_CLOSE: Boolean = true

        /** Allows both sides of double doors to open/close simultaneously. */
        var ALLOW_DOUBLE_DOORS: Boolean = true

        /** Enables knocking on doors. */
        var ALLOW_KNOCKING_DOORS: Boolean = true

        /** Enables knocking on gates. */
        var ALLOW_KNOCKING_GATES: Boolean = true

        /** Enables knocking on trapdoors. */
        var ALLOW_KNOCKING_TRAPDOORS: Boolean = true

        /** Knocking requires the player's hand to be empty. */
        var KNOCKING_REQUIRES_EMPTY_HAND: Boolean = true

        /** Players must shift (crouch) to knock. */
        var KNOCKING_REQUIRES_SHIFT: Boolean = true

        /** The delay (in milliseconds) before automatic closure. */
        var AUTO_CLOSE_DELAY: Long = 6L * 1000L // 6 seconds
    }

    /** Configuration settings for the eclipseModule. */
    object EclipseModule {
        /** Enables or disables the eclipseModule. */
        var ENABLED: Boolean = true

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

        /** The list of mobs that are excluded from the eclipse buff. */
        var EXCLUDED_MOBS: Set<EntityType> = setOf(EntityType.ENDERMAN)

        /** If creepers should be randomly powered. */
        var RANDOM_POWERED_CREEPERS: Boolean = true

        /** The title message displayed when the eclipse is active. */
        var ECLIPSE_START_TITLE: Title =
            Title.title("An Eclipse is rising!".fireFmt().mm(), "Stay inside ;)".mangoFmt().mm())

        /** The title message displayed when the eclipse is inactive. */
        var ECLIPSE_END_TITLE: Title =
            Title.title("An Eclipse is setting!".fireFmt().mm(), "You can go outside now :P".mangoFmt().mm())

        /** The sound effect used for when the eclipse is active. */
        var ECLIPSE_START_SOUND: Sound = Sound.sound(
            BukkitSound.ENTITY_WITHER_SPAWN,
            Sound.Source.HOSTILE,
            1.0f,
            1.0f
        )

        /** The sound effect used for when the eclipse is inactive. */
        var ECLIPSE_END_SOUND: Sound = Sound.sound(
            BukkitSound.ENTITY_WITHER_DEATH,
            Sound.Source.HOSTILE,
            1.0f,
            1.0f
        )

        /** The initial delay before the first eclipse. */
        var INIT_DELAY: Long = TimeUtils.seconds(0)

        /** The interval between eclipses. */
        var INTERVAL: Long = TimeUtils.seconds(10)
    }

    /** Configuration settings for the InvSearchModule. */
    object InvSearchModule {
        /** Enables or disables the InvSearchModule. */
        var ENABLED: Boolean = true

        /** The cooldown to use the mechanic again. */
        var COOLDOWN: Long = 1L * 1000L // 1 second

        /** The radius used for searching. */
        var SEARCH_RADIUS: Int = 5
    }

    /** Configuration settings for the InvUnloadModule. */
    object InvUnloadModule {
        /** Enables or disables the InvUnloadModule. */
        var ENABLED: Boolean = true

        /** The cooldown to use the mechanic again. */
        var COOLDOWN: Long = 1L * 1000L // 1 second

        /** If it should match enchantments. */
        var MATCH_ENCHANTMENTS: Boolean = true

        /** If it should match enchantments on books. */
        var MATCH_ENCHANTMENTS_ON_BOOKS: Boolean = true

        /** The sound effect used for unloading. */
        var SOUND_ON_UNLOAD: Sound = Sound.sound(
            BukkitSound.ENTITY_PLAYER_LEVELUP,
            Sound.Source.PLAYER,
            1.0f,
            1.0f
        )
    }

    /** Configuration settings for the JoinQuitModule. */
    object JoinQuitModule {
        /** Enables or disables the MotdModule. */
        var ENABLED: Boolean = true

        /** The message displayed when a player joins. */
        var WELCOME_TEXT: String =
            """
            ${"]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)}
            <image>${"⯈".mangoFmt(true)}
            <image>${"⯈".mangoFmt(true)}
            <image>${"⯈".mangoFmt(true)} ${"Welcome".fireFmt()} <player>
            <image>${"⯈".mangoFmt(true)}
            <image>${"⯈".mangoFmt(true)}
            <image>${"⯈".mangoFmt(true)} ${"Check out".fireFmt()}<gray>: ${
                "/rules".clickRunCmd(Utils.cmdHover).skylineFmt()
            } <gray>🟅 ${"/guide".clickRunCmd(Utils.cmdHover).skylineFmt()}
            <image>${"⯈".mangoFmt(true)}
            <image>${"⯈".mangoFmt(true)}
            ${"]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)}
            """.trimIndent()
    }

    /** Configuration settings for the MotdModule. */
    object MotdModule {
        /** Enables or disables the MotdModule. */
        var ENABLED: Boolean = true

        /** The message of the day, with max 2 lines. */
        val MOTD: List<String> = listOf(
            "<b>Ultimate Private SMP</b>".fireFmt(),
            "<b>➤ WELCOME BACK LADS!</b>".mangoFmt()
        )
    }

    /** Configuration settings for the RecipiesModule. */
    object RecipiesModule {
        /** Enables or disables the RecipiesModule. */
        var ENABLED: Boolean = true
    }

    /** Configuration settings for the TabListModule. */
    object TabListModule {
        /** Enables or disables the TabListModule. */
        var ENABLED: Boolean = true

        /** The initial delay before the first update. */
        var INIT_DELAY: Long = 0L

        /** The interval between updates. */
        var INTERVAL: Long = TimeUtils.seconds(10)

        /** The header of the tab list. Each element is a line. */
        var HEADER: List<String> = listOf(
            "${"]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt()}   ${"⚡ IllyriaRPG 1.21.5 ⚡".fireFmt()}   ${
                "]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)
            }",
            ""
        )

        /** The footer of the tab list. Each element is a line. */
        var FOOTER: List<String> = listOf(
            "",
            "${"]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt()}  ${"TPS:".fireFmt()} <tps> ${"|".mangoFmt()} ${
                "Weather:".fireFmt()
            } <weather>  ${
                "]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true)
            }"
        )
    }

    /** Configuration settings for the TreesModule. */
    object TreesModule {
        /** Enables or disables the TreesModule. */
        var ENABLED: Boolean = true

        /** If it should copy the biomes. */
        var COPY_BIOMES: Boolean = false

        /** If it should copy the entities. */
        var COPY_ENTITIES: Boolean = false

        /** If it should ignore air blocks. */
        var IGNORE_AIR_BLOCKS: Boolean = true

        /** If it should ignore structure void blocks. */
        var IGNORE_STRUCTURE_VOID_BLOCKS: Boolean = true

        /**
         * If a sapling type is missing here, no custom schematic will be used and default behaviour applies.
         * You can define a file, multiple files, or a folder.
         */
        var SAPLING_LINK: Map<Material, List<String>> = mapOf(
            Material.ACACIA_SAPLING to listOf("trees/acacia"),
            Material.BIRCH_SAPLING to listOf("trees/birch"),
            Material.CHERRY_SAPLING to listOf("trees/cherry"),
            Material.CRIMSON_FUNGUS to listOf("trees/crimson"),
            Material.DARK_OAK_SAPLING to listOf("trees/dark_oak"),
            Material.JUNGLE_SAPLING to listOf("trees/jungle"),
            Material.MANGROVE_PROPAGULE to listOf("trees/mangrove"),
            Material.OAK_SAPLING to listOf("trees/oak"),
            Material.PALE_OAK_SAPLING to listOf("trees/pale_oak"),
            Material.SPRUCE_SAPLING to listOf("trees/spruce"),
            Material.WARPED_FUNGUS to listOf("trees/warped"),
        )
    }
}