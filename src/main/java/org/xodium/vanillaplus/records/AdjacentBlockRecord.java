package org.xodium.vanillaplus.records;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import java.util.Objects;

/**
 * A record representing an adjacent block with specific properties.
 *
 * @param offsetX the X offset of the adjacent block
 * @param offsetZ the Z offset of the adjacent block
 * @param hinge   the hinge type of the door associated with the adjacent block
 * @param facing  the facing direction of the adjacent block
 * @throws NullPointerException if {@code hinge} or {@code facing} is null
 */
public record AdjacentBlockRecord(int offsetX, int offsetZ, Door.Hinge hinge, BlockFace facing) {
    public AdjacentBlockRecord {
        Objects.requireNonNull(hinge);
        Objects.requireNonNull(facing);
    }
}