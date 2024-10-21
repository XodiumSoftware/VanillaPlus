package org.xodium.doorsplus.interfaces;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import org.xodium.doorsplus.data.PossibleNeighbour;

public interface CONST {
    String CONFIG_FILE = "config.yml";
    String WRAPS = "wraps";
    String V_PATTERN = "\\d+\\.\\d+\\.\\d+";
    String DEBUG_PREFIX = "[DEBUG] ";
    PossibleNeighbour[] POSSIBLE_NEIGHBOURS = new PossibleNeighbour[] {
            new PossibleNeighbour(0, -1, Door.Hinge.RIGHT, BlockFace.EAST),
            new PossibleNeighbour(0, 1, Door.Hinge.LEFT, BlockFace.EAST),

            new PossibleNeighbour(1, 0, Door.Hinge.RIGHT, BlockFace.SOUTH),
            new PossibleNeighbour(-1, 0, Door.Hinge.LEFT, BlockFace.SOUTH),

            new PossibleNeighbour(0, 1, Door.Hinge.RIGHT, BlockFace.WEST),
            new PossibleNeighbour(0, -1, Door.Hinge.LEFT, BlockFace.WEST),

            new PossibleNeighbour(-1, 0, Door.Hinge.RIGHT, BlockFace.NORTH),
            new PossibleNeighbour(1, 0, Door.Hinge.LEFT, BlockFace.NORTH)
    };
}
