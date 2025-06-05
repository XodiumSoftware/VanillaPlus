/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.server.MapInitializeEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.mapify.util.Util
import org.xodium.vanillaplus.utils.Utils
import java.awt.image.BufferedImage
import java.net.URI
import javax.imageio.ImageIO

/** Represents a module handling map mechanics within the system. */
class MapModule : ModuleInterface {
    override fun enabled(): Boolean = Config.MapModule.ENABLED

    @Suppress("UnstableApiUsage")
    override fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("mapify")
                .requires { it.sender.hasPermission(Perms.Mapify.USE) }
                .executes { it -> Utils.tryCatch(it) { mapify() } }
                .then(
                    Commands.literal("refresh")
                        .requires { it.sender.hasPermission(Perms.Mapify.REFRESH) }
                        .executes { it -> Utils.tryCatch(it) { mapify() } })
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: MapInitializeEvent) {
        instance.server.scheduler.runTaskAsynchronously(instance, Runnable {
            Util.getRenderer(event.map)?.let { pluginRenderer ->
                event.map.renderers.forEach { event.map.removeRenderer(it) }
                event.map.addRenderer(pluginRenderer)
            }
        })
    }

    private fun mapify() {}

    /**
     * Fetches an image from the specified URL.
     * @param url The URL of the image to fetch.
     * @return A BufferedImage if the image was successfully fetched, null otherwise.
     */
    fun fetchImageFromUrl(url: String): BufferedImage? {
        return try {
            URI(url).toURL().openStream().use { ImageIO.read(it) }
        } catch (e: Exception) {
            instance.logger.warning("Failed to fetch image from $url : ${e.message}")
            null
        }
    }
}