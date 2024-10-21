package org.xodium.doorsplus;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.xodium.doorsplus.config.Config;
import com.google.common.base.Enums;

public class Utils {

    public static long seconds2Ticks(double seconds) {
        return Math.round(seconds * 20);
    }

    public static String loc2str(Block block) {
        return block.getX() + ", " + block.getY() + ", " + block.getZ();
    }

    public static void playKnockSound(Block block) {
        DoorsPlus plugin = DoorsPlus.getInstance();
        plugin.debug("Playing knock sound");

        Location location = block.getLocation();
        World world = block.getWorld();
        Sound sound = block.getType() == Material.IRON_DOOR
                ? Enums.getIfPresent(Sound.class, plugin.getConfig().getString(Config.SOUND_KNOCK_IRON))
                        .or(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR)
                : Enums.getIfPresent(Sound.class, plugin.getConfig().getString(Config.SOUND_KNOCK_WOOD))
                        .or(Sound.ITEM_SHIELD_BLOCK);

        SoundCategory category = Enums
                .getIfPresent(SoundCategory.class, plugin.getConfig().getString(Config.SOUND_KNOCK_CATEGORY))
                .or(SoundCategory.BLOCKS);

        float volume = (float) plugin.getConfig().getDouble(Config.SOUND_KNOCK_VOLUME, 1.0);
        float pitch = (float) plugin.getConfig().getDouble(Config.SOUND_KNOCK_PITCH, 1.0);

        world.playSound(location, sound, category, volume, pitch);

        // Debugging logs
        plugin.debug("World: " + world);
        plugin.debug("Location: " + location);
        plugin.debug("Sound: " + sound.name());
        plugin.debug("Category: " + category.name());
        plugin.debug("Volume: " + volume);
        plugin.debug("Pitch: " + pitch);
    }
}
