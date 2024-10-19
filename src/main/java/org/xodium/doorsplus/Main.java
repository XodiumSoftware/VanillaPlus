package org.xodium.doorsplus;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.xodium.doorsplus.config.Config;
import org.xodium.doorsplus.data.PossibleNeighbour;
import org.xodium.doorsplus.interfaces.MSG;
import org.xodium.doorsplus.listeners.DoorListener;

public class Main extends JavaPlugin {

  private static Main instance;
  private static final PossibleNeighbour[] possibleNeighbours = new PossibleNeighbour[] {
      new PossibleNeighbour(0, -1, Door.Hinge.RIGHT, BlockFace.EAST),
      new PossibleNeighbour(0, 1, Door.Hinge.LEFT, BlockFace.EAST),

      new PossibleNeighbour(1, 0, Door.Hinge.RIGHT, BlockFace.SOUTH),
      new PossibleNeighbour(-1, 0, Door.Hinge.LEFT, BlockFace.SOUTH),

      new PossibleNeighbour(0, 1, Door.Hinge.RIGHT, BlockFace.WEST),
      new PossibleNeighbour(0, -1, Door.Hinge.LEFT, BlockFace.WEST),

      new PossibleNeighbour(-1, 0, Door.Hinge.RIGHT, BlockFace.NORTH),
      new PossibleNeighbour(1, 0, Door.Hinge.LEFT, BlockFace.NORTH)
  };
  private boolean redstoneEnabled = false;

  public static Main getInstance() {
    return instance;
  }

  public void debug(String text) {
    if (getConfig().getBoolean(Config.DEBUG)) {
      getLogger().warning("[DEBUG] " + text);
    }
  }

  public Door getBottomDoor(Door door, Block block) {

    if (door.getHalf() == Bisected.Half.BOTTOM) {
      return door;
    }

    Block below = block.getRelative(BlockFace.DOWN);
    if (below.getType() != block.getType())
      return null; // Door is obviously broken

    if (below.getBlockData() instanceof Door) {
      return (Door) below.getBlockData();
    }

    return null; // Door is not matching
  }

  public Block getOtherPart(Door door, Block block) {
    if (door == null)
      return null;
    for (PossibleNeighbour neighbour : possibleNeighbours) {
      if (neighbour.getFacing() != door.getFacing())
        continue;
      if (neighbour.getHinge() != door.getHinge())
        continue;
      Block relative = block.getRelative(neighbour.getOffsetX(), 0, neighbour.getOffsetZ());
      if (relative.getType() != block.getType())
        continue;
      if (!(relative.getBlockData() instanceof Door))
        continue;
      Door otherDoor = ((Door) relative.getBlockData());
      if (otherDoor.getHinge() == neighbour.getHinge())
        continue;
      if (door.isOpen() != otherDoor.isOpen())
        continue;
      if (otherDoor.getFacing() != neighbour.getFacing())
        continue;
      return relative;
    }
    return null;
  }

  public boolean isDebug() {
    return getConfig().getBoolean(Config.DEBUG);
  }

  public boolean isRedstoneEnabled() {
    return redstoneEnabled;
  }

  @Override
  public void onEnable() {
    if (!Utils.isCompatibleEnv(this)) {
      getServer().getPluginManager().disablePlugin(this);
      return;
    }
    instance = this;
    Config.init();
    reload();
    Bukkit.getPluginManager().registerEvents(new DoorListener(), this);

    getLogger().info(MSG.PLUGIN_ENABLED);
  }

  public void reload() {
    saveDefaultConfig();
    reloadConfig();
    redstoneEnabled = getConfig().getBoolean(Config.CHECK_FOR_REDSTONE);
  }

  private void toggleDoor(Block otherDoorBlock, Openable otherDoor, boolean open) {
    otherDoor.setOpen(open);
    otherDoorBlock.setBlockData(otherDoor);
  }

  public void toggleOtherDoor(Block block, Block otherBlock, boolean open, boolean causedByRedstone) {
    toggleOtherDoor(block, otherBlock, open, causedByRedstone, false);
  }

  public void toggleOtherDoor(Block block, Block otherBlock, boolean open, boolean causedByRedstone, boolean force) {

    if (!(block.getBlockData() instanceof Door))
      return;
    if (!(otherBlock.getBlockData() instanceof Door))
      return;

    Door door = (Door) block.getBlockData();
    Door otherDoor = (Door) otherBlock.getBlockData();

    if (causedByRedstone) {
      toggleDoor(otherBlock, otherDoor, open);
      return;
    }

    boolean openNow = door.isOpen();
    new BukkitRunnable() {
      @Override
      public void run() {
        if (!(otherBlock.getBlockData() instanceof Door))
          return;
        Door newDoor = (Door) block.getBlockData();
        if (!force && newDoor.isOpen() == openNow) {
          return;
        }
        toggleDoor(otherBlock, otherDoor, open);
      }
    }.runTaskLater(this, 1L);

  }

  @Override
  public void onDisable() {
    getLogger().info(MSG.PLUGIN_DISABLED);
  }

}