/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.invunloadold.api

import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class InvUnloadCheckAccessEvent(
    who: Player,
    action: Action,
    item: ItemStack?,
    clickedBlock: Block?,
    clickedFace: BlockFace
) : PlayerInteractEvent(who, action, item, clickedBlock, clickedFace)
