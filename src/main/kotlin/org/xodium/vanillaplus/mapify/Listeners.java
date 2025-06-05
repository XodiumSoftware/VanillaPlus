/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.mapify;

import com.funnyboyroks.mapify.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapRenderer;

import java.util.List;

public class Listeners implements Listener {

    @EventHandler
    public void onMapInit(MapInitializeEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(Mapify.INSTANCE, () -> {
            MapRenderer pluginRenderer = Util.getRenderer(event.getMap());

            if (pluginRenderer == null) return;

            List<MapRenderer> renderers = event.getMap().getRenderers();
            renderers.forEach(event.getMap()::removeRenderer);

            event.getMap().addRenderer(pluginRenderer);
        });
    }
}
