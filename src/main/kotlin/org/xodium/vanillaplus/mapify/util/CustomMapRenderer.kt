/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.mapify.util

import org.bukkit.entity.Player
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage

abstract class CustomMapRenderer(image: Image, x: Int, y: Int, scaleX: Int, scaleY: Int) : MapRenderer() {
    private val image: BufferedImage

    init {
        val img = BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB)
        val g = img.createGraphics()
        val scaled = image.getScaledInstance(scaleX * 128, scaleY * 128, Image.SCALE_DEFAULT)
        g.drawImage(scaled, -x * 128, -y * 128, null)
        g.dispose()
        this.image = img
    }

    override fun render(map: MapView?, canvas: MapCanvas, player: Player?) {
        for (x in 0..127) {
            for (y in 0..127) {
                // _Note: this seems to be more performant than using MapCanvas#drawImage._
                val col = Color(this.image.getRGB(x, y))
                canvas.setPixelColor(x, y, col)
            }
        }
    }
}
