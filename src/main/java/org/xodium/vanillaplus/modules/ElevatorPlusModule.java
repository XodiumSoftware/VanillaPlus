package org.xodium.vanillaplus.modules;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
        if (b.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE
                && b.getRelative(BlockFace.DOWN).getType() == Material.NOTE_BLOCK) {
            Vector loc = b.getLocation().toVector();
            elevators.put(loc, loc);
            e.getPlayer().sendMessage(mm.deserialize(PREFIX + "<aqua>Elevator created at: (" + loc + ")"));
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Block b = e.getTo().getBlock();
        if (b != null && b.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            Vector loc = b.getLocation().toVector();
            if (elevators.containsKey(loc)) {
                Player p = e.getPlayer();
                Vector dest = findDestination(loc, !p.isSneaking() && p.getVelocity().getY() > 0);
                if (dest != null) {
                    Location viewloc = dest.toLocation(b.getWorld()).add(0.5, 1, 0.5);
                    viewloc.setPitch(p.getLocation().getPitch());
                    viewloc.setYaw(p.getLocation().getYaw());
                    p.teleport(viewloc);
                    p.playSound(viewloc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                    b.getWorld().spawnParticle(Particle.PORTAL, viewloc, 50, 0.5, 1, 0.5, 0.1);
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
}
