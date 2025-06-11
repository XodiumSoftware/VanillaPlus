/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.managers

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
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
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.utils.ExtUtils.il
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.Utils
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlin.reflect.full.declaredMemberProperties
import kotlin.time.Duration.Companion.seconds

/** Represents the config manager within the system. */
object ConfigManager {
    private val configPath = VanillaPlus.Companion.instance.dataFolder.toPath().resolve("config.json")
    private val objectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
        registerModule(Jdk8Module())
    }
    var data: ConfigData = ConfigData()

    /** Initializes the configuration by loading settings from the config file. */
    fun load() {
        if (Files.exists(configPath)) {
            VanillaPlus.Companion.instance.logger.info("Config: Loading settings.")
            data = objectMapper.readValue(Files.readString(configPath))
            VanillaPlus.Companion.instance.logger.info("Config: Settings loaded successfully.")
        } else {
            VanillaPlus.Companion.instance.logger.info("Config: No config file found, creating new config.")
            save()
        }
    }

    /** Saves the current settings to the config file. */
    private fun save() {
        VanillaPlus.Companion.instance.logger.info("Config: Saving settings.")
        Files.createDirectories(configPath.parent)
        Files.writeString(
            configPath,
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
        VanillaPlus.Companion.instance.logger.info("Config: Settings saved successfully.")
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
        val modules = ConfigData::class.declaredMemberProperties.mapNotNull { prop ->
            prop.get(data)?.let { module -> prop to module }
        }
        return buildGui {
            containerType = chestContainer { rows = dynamicRowsCalculator(modules.size) }
            spamPreventionDuration = 1.seconds
            title("<b>VanillaPlus Config</b>".fireFmt().mm())
            statelessComponent { inv ->
                modules.forEachIndexed { idx, (prop, _) ->
                    inv[idx] = ItemBuilder.from(
                        guiItem(
                            Material.WRITABLE_BOOK,
                            prop.name.replaceFirstChar { it.uppercaseChar() }.mangoFmt(),
                            listOf("<i>Click to open module settings</i>".fireFmt())
                        )
                    ).asGuiItem { player, _ -> moduleGui(prop.get(data) ?: "").open(player) }
                }
            }
        }
    }

    /**
     * Creates a GUI for a specific module.
     * @param module The module for which the GUI is created.
     * @return A Gui object representing the module GUI.
     */
    private fun moduleGui(module: Any): Gui {
        val props = module::class.declaredMemberProperties
        return buildGui {
            containerType = chestContainer { rows = dynamicRowsCalculator(props.size) }
            spamPreventionDuration = 1.seconds
            title("<b>${module::class.simpleName}</b>".fireFmt().mm())
            statelessComponent { inv ->
                props.forEachIndexed { idx, prop ->
                    inv[idx] = ItemBuilder.from(
                        guiItem(
                            Material.PAPER,
                            prop.name.replaceFirstChar { it.uppercaseChar() }.mangoFmt(),
                            listOf("<gray>Value:</gray> ${prop.getter.call(module).toString().fireFmt()}")
                        )
                    ).asGuiItem { _, _ -> }
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

    /**
     * Calculates the number of rows needed for a dynamic GUI based on the number of items.
     * @param size The number of items.
     * @return The number of rows needed.
     */
    private fun dynamicRowsCalculator(size: Int): Int = ((size - 1) / 9 + 1).coerceIn(1, 6)
}