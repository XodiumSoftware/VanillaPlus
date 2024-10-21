package org.xodium.doorsplus;

import org.bukkit.block.Block;

public class Utils {

    public static long seconds2Ticks(double seconds) {
        return Math.round(seconds * 20);
    }

    public static String loc2str(Block block) {
        return block.getX() + ", " + block.getY() + ", " + block.getZ();
    }
}
