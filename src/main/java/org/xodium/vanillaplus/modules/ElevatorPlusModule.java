package org.xodium.vanillaplus.modules;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class ElevatorPlusModule implements Listener {
    private final Map<Vector, Vector> elevators = new HashMap<>();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Block b = e.getBlock();
        if (b.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            Block below = b.getRelative(BlockFace.DOWN);
            if (below.getType() == Material.NOTE_BLOCK) {
                Vector loc = b.getLocation().toVector();
                elevators.put(loc, loc);
                e.getPlayer().sendMessage("Elevator created at " + loc);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Block b = e.getTo().getBlock();
        if (b != null && b.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            Vector loc = b.getLocation().toVector();
            if (elevators.containsKey(loc)) {
                if (e.getPlayer().isSneaking()) {
                    Vector dest = findDestination(loc, false);
                    if (dest != null) {
                        e.getPlayer().teleport(dest.toLocation(b.getWorld()).add(0.5, 1, 0.5));
                    } else {
                        e.getPlayer().sendMessage("No corresponding elevator found below.");
                    }
                } else if (e.getPlayer().getVelocity().getY() > 0) {
                    Vector dest = findDestination(loc, true);
                    if (dest != null) {
                        e.getPlayer().teleport(dest.toLocation(b.getWorld()).add(0.5, 1, 0.5));
                    } else {
                        e.getPlayer().sendMessage("No corresponding elevator found above.");
                    }
                }
            }
        }
    }

    private Vector findDestination(Vector currentLoc, boolean goingUp) {
        for (Vector loc : elevators.keySet()) {
            if (!loc.equals(currentLoc) && loc.getX() == currentLoc.getX()
                    && loc.getZ() == currentLoc.getZ()) {
                if (goingUp && loc.getY() > currentLoc.getY()) {
                    return loc;
                } else if (!goingUp && loc.getY() < currentLoc.getY()) {
                    return loc;
                }
            }
        }
        return null;
    }
}
