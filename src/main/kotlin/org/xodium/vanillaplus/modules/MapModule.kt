/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.Utils
import java.awt.Image
import java.awt.image.BufferedImage
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO

//TODO: Implement persistent map color cache storage.
//TODO: On startup, check if a cache file exists on disk.
//TODO: If cache file does not exist, build the cache and save it to disk.
//TODO: On subsequent startups, load the cache from the file to avoid rebuilding.
//TODO: Write back to disk if map colors change or cache is updated.

/** Represents a module handling map mechanics within the system. */
class MapModule : ModuleInterface {
    override fun enabled(): Boolean = Config.MapModule.ENABLED

    @Suppress("UnstableApiUsage")
    override fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("mapify")
                .requires { it.sender.hasPermission(Perms.Mapify.USE) }
                .then(
                    Commands.argument("url", StringArgumentType.string())
                        .executes { ctx ->
                            Utils.tryCatch(ctx) {
                                mapify(
                                    it.sender as Player,
                                    StringArgumentType.getString(ctx, "url")
                                )
                            }
                        }
                )
        )
    }

    init {
        if (enabled()) {
            val mapsDir = Paths.get(instance.dataFolder.toString(), "maps")
            if (Files.exists(mapsDir)) {
                Files.newDirectoryStream(mapsDir).use { stream ->
                    for (path in stream) {
                        val mapId = path.fileName.toString().substringBeforeLast('.').toIntOrNull() ?: continue
                        val mapView = instance.server.getMap(mapId) ?: continue
                        Files.newInputStream(path).use { input ->
                            val image = ImageIO.read(input)
                            renderImageToMap(mapView, image)
                        }
                    }
                }
            }
        }
    }

    /**
     * Maps the given URL to the player's held map item.
     * @param player The player holding the map item.
     * @param url The URL of the image to be mapped onto the map item.
     */
    private fun mapify(player: Player, url: String) {
        val item = player.inventory.itemInMainHand

        if (item.type != Material.FILLED_MAP) {
            player.sendMessage("You must hold a map in your hand to use this command.".mm())
            return
        }

        val itemMeta = item.itemMeta as? MapMeta ?: run {
            player.sendMessage("Invalid map item.".mm())
            return
        }
        val mapView = itemMeta.mapView ?: run {
            player.sendMessage("Could not get map view.".mm())
            return
        }
        val image = fetchAndResizeImage(url) ?: run {
            player.sendMessage("Failed to fetch or process image.".mm())
            return
        }

        renderImageToMap(mapView, image)

        val mapsDir = Paths.get(instance.dataFolder.toString(), "maps")
        Files.createDirectories(mapsDir)
        val mapId = mapView.id
        val file = mapsDir.resolve("$mapId.png")
        try {
            Files.newOutputStream(file).use { out ->
                ImageIO.write(image as BufferedImage, "PNG", out)
            }
        } catch (e: Exception) {
            player.sendMessage("Failed to save map image. See logs!".mm())
            instance.logger.severe("Failed to save map image: ${e.printStackTrace()}")
        }

        player.sendMessage("Map updated with image from URL.".mm())
    }

    /**
     * Fetches an image from the given URL and resizes it to fit a map.
     * @param url The URL of the image to fetch.
     * @return A resized image suitable for rendering on a map, or null if the fetch fails.
     */
    private fun fetchAndResizeImage(url: String): Image? {
        return try {
            URI.create(url).toURL().openConnection().apply {
                connectTimeout = 5000
                readTimeout = 5000
            }.getInputStream().use { input ->
                ImageIO.read(input)?.let { original ->
                    BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB).apply {
                        createGraphics().apply {
                            drawImage(original.getScaledInstance(128, 128, Image.SCALE_SMOOTH), 0, 0, null)
                            dispose()
                        }
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Renders the given image onto the specified map view.
     * @param mapView The map view to render the image onto.
     * @param image The image to render.
     */
    private fun renderImageToMap(mapView: MapView, image: Image) {
        mapView.renderers.forEach { mapView.removeRenderer(it) }
        mapView.addRenderer(object : MapRenderer() {
            private var rendered = false

            override fun render(map: MapView, canvas: MapCanvas, player: Player) {
                if (rendered) return
                canvas.drawImage(0, 0, image)
                rendered = true
            }
        })
    }
}