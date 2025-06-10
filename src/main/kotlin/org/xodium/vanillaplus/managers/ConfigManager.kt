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
            //TODO, "ENABLED" might be wrong.
            val enabledProp = kClass.declaredMemberProperties.find { it.name == "ENABLED" }
            if (enabledProp != null) {
                enabledProp.isAccessible = true
                val obj = kClass.objectInstance ?: return@mapNotNull null
                val value = enabledProp.getter.call(obj)
                if (value is Boolean || value is String) kClass to enabledProp else null
            } else null
        }
        val dynamicRows = modules.size.let { ((it - 1) / 9 + 1).coerceIn(1, 6) }
        return buildGui {
            containerType = chestContainer { rows = dynamicRows }
            spamPreventionDuration = 1.seconds
            title("<b>Config</b>".fireFmt().mm())
            statelessComponent { inv ->
                modules.forEachIndexed { idx, (kClass, enabledProp) ->
                    val obj = kClass.objectInstance ?: return@forEachIndexed
                    val value = enabledProp.getter.call(obj)
                    val (mat, lore) = when (value) {
                        is Boolean -> {
                            (if (value) Material.GREEN_WOOL else Material.RED_WOOL) to listOf(
                                if (value) "<green>Enabled<reset>" else "<red>Disabled<reset>",
                                "*Requires server restart*"
                            )
                        }

                        is String -> {
                            Material.BLUE_WOOL to listOf(
                                "<aqua>Value: <white>${value}<reset>",
                                "*Requires server restart*"
                            )
                        }

                        else -> {
                            Material.GRAY_WOOL to listOf("<gray>Unsupported type<reset>")
                        }
                    }
                    val name = kClass.simpleName ?: "Unknown"
                    inv[idx] = ItemBuilder.from(guiItem(mat, name.mangoFmt(), lore))
                        .asGuiItem { player, _ ->
                            val mutableProp = enabledProp as? KMutableProperty1<*, *>
                            when (value) {
                                is Boolean -> {
                                    mutableProp?.setter?.call(obj, !value)
                                    save()
                                    gui().open(player)
                                }

                                is String -> {
                                    player.sendMessage("Feature to edit String values is not implemented yet.")
                                }
                            }
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
}