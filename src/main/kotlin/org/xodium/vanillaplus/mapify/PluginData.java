/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.mapify;

import java.util.HashMap;
import java.util.Map;

public class PluginData {

    public final Map<Integer, MapData> mapData;

    public PluginData() {
        this.mapData = new HashMap<>();
    }

    public static class MapData {

        public final String url;
        public final int x;
        public final int y;
        public final int scaleX;
        public final int scaleY;

        public MapData(String url, int x, int y, int scaleX, int scaleY) {
            this.url = url;
            this.x = x;
            this.y = y;
            this.scaleX = scaleX;
            this.scaleY = scaleY;

        }
    }

}
