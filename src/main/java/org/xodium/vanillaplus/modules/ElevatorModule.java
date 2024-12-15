package org.xodium.vanillaplus.modules;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.xodium.vanillaplus.Database;
import org.xodium.vanillaplus.interfaces.ModuleInterface;

public class ElevatorModule implements ModuleInterface {
    private final String cn = getClass().getSimpleName();
    private static final Database DB = new Database();
    private static final double TELEPORT_OFFSET = 0.5;
    private static final int PARTICLE_COUNT = 20;
    private static final float SOUND_VOLUME = 1f;
    private static final float SOUND_PITCH = 1f;
    private static final int UP_INCREMENT = 1;
    private static final int DOWN_INCREMENT = -1;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Block b = p.getLocation().getBlock().getRelative(BlockFace.DOWN);
        if (isElevator(b)) {
            handleElevatorMovement(p);
        }
    }

    private void handleElevatorMovement(Player p) {
        if (p.getVelocity().getY() > 0) {
            detectOtherElevator(p, true);
        } else if (p.isSneaking()) {
            detectOtherElevator(p, false);
        }
    }

    private static boolean isElevator(Block b) {
        return b.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE
                && b.getRelative(BlockFace.DOWN).getType() == Material.NOTE_BLOCK;
    }

    private void detectOtherElevator(Player p, boolean goingUp) {
        World w = p.getWorld();
        int increment = goingUp ? UP_INCREMENT : DOWN_INCREMENT;
        int x = p.getLocation().getBlockX();
        int y = p.getLocation().getBlockY() + increment;
        int z = p.getLocation().getBlockZ();

        while (y >= w.getMinHeight() && y <= w.getMaxHeight()) {
            Block b = w.getBlockAt(x, y, z);
            if (isElevator(b) && hasEnoughSpace(b)) {
                teleportPlayer(p, b.getLocation());
                return;
            }
            y += increment;
        }
    }

    private static boolean hasEnoughSpace(Block b) {
        Block above = b.getRelative(BlockFace.UP);
        return above.getType() == Material.AIR && above.getRelative(BlockFace.UP).getType() == Material.AIR;
    }

    private void teleportPlayer(Player p, Location l) {
        Location tpl = l.add(TELEPORT_OFFSET, 1, TELEPORT_OFFSET);
        playTeleportEffects(p);
        p.teleport(tpl);
        p.playSound(tpl, Sound.ENTITY_ENDERMAN_TELEPORT, SOUND_VOLUME, SOUND_PITCH);
        playTeleportEffects(p);
    }

    private void playTeleportEffects(Player p) {
        p.getWorld().spawnParticle(Particle.PORTAL, p.getLocation(), PARTICLE_COUNT);
    }

    @Override
    public boolean enabled() {
        return DB.getData(cn + CONFIG.ENABLE, Boolean.class);
    }

    @Override
    public void config() {
        DB.setData(cn + CONFIG.ENABLE, true);
    }
}
