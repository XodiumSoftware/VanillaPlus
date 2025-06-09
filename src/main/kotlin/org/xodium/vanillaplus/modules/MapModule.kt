/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.MapId
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Executors
import javax.imageio.ImageIO

//TODO: add way to spread image across multiple maps.
//TODO: move away from using cmds for this, instead lets use tags or something similar in combination with a map.
//TODO: where the tag has the URL, and the map renderer reads that tag and fetches the image.
//TODO: has to be combined in an anvil.

/** Represents a module handling map mechanics within the system. */
class MapModule : ModuleInterface {
    override fun enabled(): Boolean = Config.MapModule.ENABLED

    private val mapSaveExecutor = Executors.newSingleThreadExecutor()

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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        if (!enabled()) return
        if (event.inventory.type == InventoryType.ANVIL) {
            val slot1 = event.inventory.getItem(0)
            val slot2 = event.inventory.getItem(1)
            val result = event.currentItem

            if (slot1?.type == Material.MAP && slot2?.type == Material.NAME_TAG && result?.type == Material.FILLED_MAP) {
                if (slot2.itemMeta?.hasCustomName() != true) return
                val tag = slot2.itemMeta?.customName() ?: return
                val url = PlainTextComponentSerializer.plainText().serialize(tag)
                val player = event.whoClicked as? Player ?: return

                event.isCancelled = true
                event.inventory.clear()

                @Suppress("UnstableApiUsage")
                val mapId = slot1.getData(DataComponentTypes.MAP_ID) ?: return

                @Suppress("UnstableApiUsage")
                val mapView = instance.server.getMap(mapId.id()) ?: return

                if (slot1.type == Material.FILLED_MAP) mapify(mapView, url)

                player.inventory.addItem(map(mapId))
            }
        }
    }

    /**
     * Maps the given URL to the player's held map item.
     * @param mapView The MapView to associate with the ItemStack.
     * @param url The URL of the image to be mapped onto the map item.
     */
    private fun mapify(mapView: MapView, url: String) {
        val image = fetchAndResizeImage(url) ?: run { return }

        renderImageToMap(mapView, image)

        val file = Paths.get(instance.dataFolder.toString(), "maps")
            .also { Files.createDirectories(it) }
            .resolve("${mapView.id}.png")

        saveMapImageAsync(image as BufferedImage, file)
    }

    /**
     * Creates a new map item stack.
     * @param mapId The ID of the map to be created.
     * @return A new ItemStack representing a filled map.
     */
    @Suppress("UnstableApiUsage")
    private fun map(mapId: MapId): ItemStack {
        return ItemStack.of(Material.FILLED_MAP).apply {
            setData(DataComponentTypes.MAP_ID, mapId)
        }
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

    /**
     * Asynchronously saves the map image to a file.
     * @param image The image to save.
     * @param file The path to the file where the image will be saved.
     */
    private fun saveMapImageAsync(image: BufferedImage, file: Path) {
        mapSaveExecutor.submit {
            val baos = ByteArrayOutputStream()
            ImageIO.write(image, "PNG", baos)
            val newBytes = baos.toByteArray()
            val shouldSave = !Files.exists(file) || !Files.readAllBytes(file).contentEquals(newBytes)
            if (shouldSave) Files.newOutputStream(file).use { it.write(newBytes) }
        }
    }
}