package org.xodium.vanillaplus.modules;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.xodium.vanillaplus.VanillaPlus;
import org.xodium.vanillaplus.interfaces.MSG;

import net.kyori.adventure.text.minimessage.MiniMessage;

public class ElevatorPlusModule implements Listener, MSG {
    private final VanillaPlus vp = VanillaPlus.getInstance();
    private final Map<Vector, Vector> elevators = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    {
        loadElevators();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Block b = e.getBlock();
        if (b.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE
                && b.getRelative(BlockFace.DOWN).getType() == Material.NOTE_BLOCK) {
            Vector loc = b.getLocation().toVector();
            elevators.put(loc, loc);
            e.getPlayer().sendMessage(mm.deserialize(PREFIX + "<aqua>Elevator created at: (" + loc + ")"));
            saveElevators();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Block b = e.getTo().getBlock();
        Player p = e.getPlayer();
        if (b != null && b.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            Vector loc = b.getLocation().toVector();
            if (elevators.containsKey(loc)) {
                boolean goingUp = !p.isSneaking() && p.getVelocity().getY() > 0;
                Vector dest = findDestination(loc, goingUp);
                if (dest != null) {
                    p.teleport(dest.toLocation(b.getWorld()).add(0.5, 1, 0.5));
                }
            }
        }
    }

    private Vector findDestination(Vector currentLoc, boolean goingUp) {
        return elevators.keySet().stream()
                .filter(loc -> !loc.equals(currentLoc) && loc.getX() == currentLoc.getX()
                        && loc.getZ() == currentLoc.getZ())
                .filter(loc -> goingUp ? loc.getY() > currentLoc.getY() : loc.getY() < currentLoc.getY())
                .findFirst()
                .orElse(null);
    }

    // TODO: make database instead.
    private void saveElevators() {
        File file = new File(vp.getDataFolder(), "elevators.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("elevators", elevators.keySet().stream().map(Vector::toString).collect(Collectors.toList()));
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadElevators() {
        File file = new File(vp.getDataFolder(), "elevators.yml");
        if (!file.exists()) {
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.getStringList("elevators").forEach(loc -> {
            String[] parts = loc.replace("(", "").replace(")", "").split(",");
            Vector vector = new Vector(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]));
            elevators.put(vector, vector);
        });
    }
}
