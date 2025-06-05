/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.utils

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapRenderer
import org.xodium.vanillaplus.data.MapData
import org.xodium.vanillaplus.mapify.util.Util.getRenderer
import org.xodium.vanillaplus.mapify.util.Util.getUrl
import java.util.function.Consumer

/** Map utilities. */
object MapUtils {
    fun getMaps(url: String, width: Int, height: Int): MutableList<ItemStack?>? {
        ArrayList<ItemStack?>()
        val u = getUrl(url)
        if (u == null) return null
        return null
    }

    private fun createMap(url: String?, x: Int, y: Int, w: Int, h: Int): ItemStack? {
        val stack = ItemStack(Material.FILLED_MAP)
        val meta = checkNotNull(stack.itemMeta as MapMeta)
        val view = Bukkit.getServer().createMap(Bukkit.getServer().worlds[0])
        Mapify.INSTANCE.dataHandler!!.data?.mapData?.put(view.id, MapData(url, x, y, w, h))
        Mapify.INSTANCE.dataHandler!!.dirty()

        view.renderers.forEach(Consumer { renderer: MapRenderer? -> view.removeRenderer(renderer) })
        val renderer = getRenderer(view)
        if (renderer == null) return null
        view.addRenderer(renderer)

        meta.mapView = view

        var lore = meta.lore
        lore = lore ?: ArrayList()
        lore.add(0, "Position: ($x, $y)")
        meta.lore = lore

        stack.setItemMeta(meta)
        return stack
    }
}