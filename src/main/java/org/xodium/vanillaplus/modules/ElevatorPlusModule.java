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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.xodium.vanillaplus.interfaces.MSG;

import net.kyori.adventure.text.minimessage.MiniMessage;

// TODO: add way to save the elevators to a file/db.
// TODO: add safety when teleporting.
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
            e.getPlayer().sendMessage(mm.deserialize(PREFIX + "<aqua>Elevator created at: <dark_gray>(" + loc + ")"));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.PHYSICAL)
            return;

        Block b = e.getClickedBlock();
        if (b == null || !isElevatorBlock(b))
            return;

        Player p = e.getPlayer();
        if (p.isSneaking()) {
            teleportPlayer(p, b, false);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Block b = p.getLocation().getBlock();

        if (!isPlayerOnGround(p) || !isElevatorBlock(b))
            return;

        if (p.getVelocity().getY() > 0) {
            teleportPlayer(p, b, true);
        }
    }

    // private boolean isSafeToTeleport(Location loc) {
    // Block b = loc.getBlock();
    // Block bAbove = b.getRelative(BlockFace.UP);
    // return b.isEmpty() && bAbove.isEmpty();
    // }

    private boolean isPlayerOnGround(Player p) {
        Location loc = p.getLocation();
        loc.setY(loc.getY() - 0.1);
        return loc.getBlock().getType().isSolid();
    }

    private boolean isElevatorBlock(Block b) {
        return b.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE
                && b.getRelative(BlockFace.DOWN).getType() == Material.NOTE_BLOCK;
    }

    private void teleportPlayer(Player p, Block b, boolean goingUp) {
        Vector loc = b.getLocation().toVector();
        if (!elevators.containsKey(loc))
            return;

        Vector dest = findDestination(loc, goingUp);
        if (dest != null) {
            Location viewloc = dest.toLocation(b.getWorld()).add(0.5, 1, 0.5);
            viewloc.setPitch(p.getLocation().getPitch());
            viewloc.setYaw(p.getLocation().getYaw());
            p.teleport(viewloc);
            p.playSound(viewloc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            b.getWorld().spawnParticle(Particle.PORTAL, viewloc, 50, 0.5, 1, 0.5, 0.1);
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
