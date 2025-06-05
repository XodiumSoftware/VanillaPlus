/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */
package org.xodium.vanillaplus.mapify.util

import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import org.xodium.vanillaplus.mapify.Mapify
import java.awt.Image
import java.awt.Point
import java.awt.image.RenderedImage
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.imageio.ImageIO

object Util {
    fun getUrl(arg: String): URL? {
        return try {
            URL(arg)
        } catch (_: MalformedURLException) {
            null
        }
    }

    fun getImage(url: URL): Image? {
        val imgFile = getImageFile(url)
        Mapify.INSTANCE.config?.let {
            if (it.saveImages) {
                if (imgFile.exists()) {
                    try {
                        return ImageIO.read(imgFile)
                    } catch (_: IOException) {
                        Mapify.INSTANCE.logger.severe(
                            String.format(
                                "Invalid image on disk: %s\n\tThis file should be removed by the server owner.  Downloading image from url...",
                                imgFile.path
                            )
                        )
                        // This does not return so it downloads the image
                    }
                } else {
                    if (Mapify.INSTANCE.config!!.debug) {
                        Mapify.INSTANCE.logger.info("File not found on system.  Downloading...")
                    }
                }
            }
        }
        try {
            val image: Image? = ImageIO.read(url)
            Mapify.INSTANCE.config?.let {
                if (it.saveImages) {
                    ImageIO.write(image as RenderedImage?, "png", imgFile)
                }
            }
            return image
        } catch (e: IOException) {
            e.printStackTrace()
            Mapify.INSTANCE.logger.severe("Invalid image url: $url")
            return null
        }
    }

    private fun getImageFile(url: URL): File {
        // SHA-256 is supposed to be collision proof (I think) and the actual result doesn't matter, as long as it's consistent
        val md: MessageDigest
        try {
            md = MessageDigest.getInstance("SHA-256")
        } catch (ex: NoSuchAlgorithmException) {
            // This should only be called if the config is enabled, so it's okay to throw
            // Possible TODO: Convert this to use a different method?
            //     The concern with ^ is that is loses its consistency guarantee
            throw RuntimeException(ex)
        }
        val bytes = md.digest(url.toString().toByteArray(StandardCharsets.UTF_8))
        var name = bytesToString(bytes)

        // It should always be .png
        name += ".png"

        return Paths.get(Mapify.INSTANCE.dataFolder.path, "img", name).toFile()
    }

    fun getMaps(url: String, width: Int, height: Int): MutableList<ItemStack?>? {
        ArrayList<ItemStack?>()

        val u = getUrl(url)
        if (u == null) return null
        /* TODO: class org.jetbrains.kotlin.nj2k.types.JKJavaNullPrimitiveType */Mapify.INSTANCE.imageCache!!.get(u)
        return null
    }

    fun tryParseInt(s: String): Int? {
        return try {
            s.toInt()
        } catch (ex: Exception) {
            null
        }
    }

    private fun getDimensions(str: String): Point? {
        try {
            val parts: Array<String?> = str.split("x".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val w = parts[0]!!.toInt()
            val h = parts[1]!!.toInt()
            return Point(w, h)
        } catch (ex: Exception) {
            return null
        }
    }

    fun giveItems(player: Player, vararg stacks: ItemStack?) {
        val overflow = player.inventory.addItem(*stacks)

        if (overflow.isEmpty()) return

        player.sendMessage(
            ChatColor.RED.toString() + String.format(
                "You were given %d item%s, but only %d fit. The others have been dropped on the ground.",
                stacks.size,
                if (stacks.size == 1) "" else "s",
                stacks.size - overflow.size
            )
        )
        overflow.forEach { (_: Int?, stack: ItemStack?) -> player.world.dropItem(player.location, stack!!) }
    }

    fun getRenderer(view: MapView): MapRenderer? {
        val data = Mapify.INSTANCE.dataHandler!!.data?.mapData?.get(view.id)
        Mapify.INSTANCE.dataHandler!!.dirty()

        if (data == null) return null

        val img: Image? = Mapify.INSTANCE.imageCache!!.get(getUrl(data.url!!))
        if (img == null) return null
        return CustomMapRenderer(img, data.x, data.y, data.scaleX, data.scaleY)
    }

    private fun bytesToString(bytes: ByteArray): String {
        val out = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            if (b <= 0x0f) {
                out.append("0")
            }
            out.append(Integer.toHexString(0xff and b.toInt()))
        }
        return out.toString()
    }

    fun dimsMatch(dims: Point, max: String): Boolean {
        // "" means no filter
        if (max.isBlank()) return true

        // WxH
        if (max.contains("x")) {
            // dims
            val bounds = getDimensions(max)
            if (bounds == null) return false // invalid dims

            return dims.x <= bounds.x && dims.y <= bounds.y
        }

        // N
        val area: Int
        try {
            area = max.toInt()
        } catch (_: NumberFormatException) {
            return false // invalid number
        }

        return area <= 0 || dims.x * dims.y <= area
    }
}
