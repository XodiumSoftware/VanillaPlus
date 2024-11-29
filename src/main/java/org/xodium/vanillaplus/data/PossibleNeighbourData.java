package org.xodium.vanillaplus.data;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;

import java.util.Objects;

public final class PossibleNeighbourData {
    private final BlockFace facing;
    private final Door.Hinge hinge;
    private final int offsetX;
    private final int offsetZ;

    public PossibleNeighbourData(int offsetX, int offsetZ, Door.Hinge hinge, BlockFace facing) {
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.hinge = Objects.requireNonNull(hinge, "hinge cannot be null");
        this.facing = Objects.requireNonNull(facing, "facing cannot be null");
    }

    public BlockFace getFacing() {
        return facing;
    }

    public Door.Hinge getHinge() {
        return hinge;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetZ() {
        return offsetZ;
    }

    @Override
    public String toString() {
        return "PossibleNeighbour{" +
                "offsetX=" + offsetX +
                ", offsetZ=" + offsetZ +
                ", facing=" + facing +
                ", hinge=" + hinge +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PossibleNeighbourData))
            return false;
        PossibleNeighbourData that = (PossibleNeighbourData) o;
        return offsetX == that.offsetX &&
                offsetZ == that.offsetZ &&
                facing == that.facing &&
                hinge == that.hinge;
    }

    @Override
    public int hashCode() {
        return Objects.hash(facing, hinge, offsetX, offsetZ);
    }
}