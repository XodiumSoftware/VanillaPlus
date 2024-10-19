package org.xodium.doorsplus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.xodium.doorsplus.config.Config;
import org.xodium.doorsplus.interfaces.CONST;
import org.xodium.doorsplus.interfaces.DEP;
import org.xodium.doorsplus.interfaces.MSG;

import com.google.common.base.Enums;

public class Utils {

    public static boolean isCompatibleEnv(JavaPlugin plugin) {
        String version = plugin.getServer().getVersion();
        Pattern pattern = Pattern.compile(CONST.V_PATTERN);
        Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            String serverVersion = matcher.group();
            if (!serverVersion.equals(DEP.V)) {
                plugin.getLogger().severe(MSG.WRONG_VERSION);
                return false;
            }
        } else {
            plugin.getLogger().severe(MSG.WRONG_VERSION);
            return false;
        }
        return true;
    }

    public static long seconds2Ticks(double seconds) {
        return (long) seconds * 20;
    }

    public static String loc2str(Block block) {
        String stringBuilder = block.getX() +
                ", " +
                block.getY() +
                ", " +
                block.getZ();
        return stringBuilder;
    }

    public static void playKnockSound(Block block) {
        Main.getInstance().debug("Finally playing sound");
        Main main = Main.getInstance();
        Location location = block.getLocation();
        World world = block.getWorld();
        Sound sound = block.getType() == Material.IRON_DOOR
                ? Enums.getIfPresent(Sound.class, main.getConfig().getString(Config.SOUND_KNOCK_IRON))
                        .or(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR)
                : Enums.getIfPresent(Sound.class, main.getConfig().getString(Config.SOUND_KNOCK_WOOD))
                        .or(Sound.ITEM_SHIELD_BLOCK);
        SoundCategory category = Enums
                .getIfPresent(SoundCategory.class, main.getConfig().getString(Config.SOUND_KNOCK_CATEGORY))
                .or(SoundCategory.BLOCKS);
        float volume = (float) main.getConfig().getDouble(Config.SOUND_KNOCK_VOLUME);
        float pitch = (float) main.getConfig().getDouble(Config.SOUND_KNOCK_PITCH);
        world.playSound(location, sound, category, volume, pitch);
        Main.getInstance().debug("World: " + world);
        Main.getInstance().debug("Location: " + location);
        Main.getInstance().debug("Sound: " + sound.name());
        Main.getInstance().debug("Category: " + category.name());
        Main.getInstance().debug("Volume: " + volume);
        Main.getInstance().debug("Pitch: " + pitch);
    }
}
