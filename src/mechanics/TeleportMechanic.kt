package org.xodium.illyriaplus.mechanics

import org.xodium.illyriaplus.interfaces.MechanicInterface
import org.xodium.illyriaplus.items.TeleportScrollItem

/** Handles teleportation mechanics within the system. */
internal object TeleportMechanic : MechanicInterface {
    /** Reference to the Teleport Scroll item. */
    val item = TeleportScrollItem.item
}
