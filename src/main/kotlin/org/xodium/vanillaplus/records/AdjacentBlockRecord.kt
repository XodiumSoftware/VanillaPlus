package org.xodium.vanillaplus.records;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import java.util.Objects;

public record AdjacentBlockRecord(int offsetX, int offsetZ, Door.Hinge hinge, BlockFace facing) {
    public AdjacentBlockRecord {
        Objects.requireNonNull(hinge);
        Objects.requireNonNull(facing);
    }
}