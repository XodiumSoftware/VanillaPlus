package org.xodium.vanillaplus.modules;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.xodium.vanillaplus.interfaces.MSG;

import net.kyori.adventure.text.minimessage.MiniMessage;

public class ElevatorPlusModule implements Listener, MSG {
    private final Map<Vector, Vector> elevators = new HashMap<>();
    private final MiniMessage mm = MiniMessage.miniMessage();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Block b = e.getBlock();
        Player p = e.getPlayer();
        if (b.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            Block below = b.getRelative(BlockFace.DOWN);
            if (below.getType() == Material.NOTE_BLOCK) {
                Vector loc = b.getLocation().toVector();
                elevators.put(loc, loc);
                p.sendMessage(mm.deserialize(PREFIX + "<aqua>Elevator created at: (" + loc + ")"));
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Block b = e.getTo().getBlock();
        Player p = e.getPlayer();
        if (b != null && b.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            Vector loc = b.getLocation().toVector();
            if (elevators.containsKey(loc)) {
                if (p.isSneaking()) {
                    Vector dest = findDestination(loc, false);
                    if (dest != null) {
                        p.teleport(dest.toLocation(b.getWorld()).add(0.5, 1, 0.5));
                    } else {
                        p.sendMessage(mm.deserialize(PREFIX + "<yellow>No corresponding elevator found below."));
                    }
                } else if (p.getVelocity().getY() > 0) {
                    Vector dest = findDestination(loc, true);
                    if (dest != null) {
                        p.teleport(dest.toLocation(b.getWorld()).add(0.5, 1, 0.5));
                    } else {
                        p.sendMessage(mm.deserialize(PREFIX + "<yellow>No corresponding elevator found above."));
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
