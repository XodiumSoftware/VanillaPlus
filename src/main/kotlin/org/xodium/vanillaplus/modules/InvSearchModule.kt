/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.block.DoubleChest
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.invunload.BlockUtils
import org.xodium.vanillaplus.utils.invunload.InvUtils
import org.xodium.vanillaplus.utils.invunload.PlayerUtils

class InvSearchModule : ModuleInterface {
    override fun enabled(): Boolean = Config.InvSearchModule.ENABLED

    @Suppress("UnstableApiUsage")
    override fun cmd(): LiteralArgumentBuilder<CommandSourceStack>? {
        return Commands.literal("invsearch")
            .requires { it.sender.hasPermission(Perms.InvUnload.USE) }
            .then(
                Commands.argument("radius", IntegerArgumentType.integer(1))
                    .then(
                        Commands.argument("material", StringArgumentType.word())
                            .executes { ctx -> handleSearch(ctx) }
                    )
                    .executes { ctx -> handleSearch(ctx) }
            )
            .then(
                Commands.argument("material", StringArgumentType.word())
                    .executes { ctx -> handleSearch(ctx) }
            )
            .executes { ctx -> handleSearch(ctx) }
    }

    @Suppress("UnstableApiUsage")
    private fun handleSearch(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as? Player ?: return 0
        val radius = runCatching { IntegerArgumentType.getInteger(ctx, "radius") }.getOrNull()
        val matName = runCatching { StringArgumentType.getString(ctx, "material") }.getOrNull()
        val mat = matName?.let { Material.getMaterial(it.uppercase()) } ?: player.inventory.itemInMainHand.type
        search(player, radius, mat)
        return 1
    }

    private fun search(player: Player, radius: Int?, mat: Material?) {
        if (mat == null) {
            player.sendMessage("You must specify a valid material or hold something in your hand.")
            return
        }
        val effectiveRadius = radius ?: 5
        val chests = BlockUtils.findChestsInRadius(player.location, effectiveRadius)
        BlockUtils.sortBlockListByDistance(chests, player.location)

        val useableChests = ArrayList<Block>()
        for (block in chests) {
            if (block != null && PlayerUtils.canPlayerUseChest(block, player)) {
                useableChests.add(block)
            }
        }

        if (useableChests.isEmpty()) {
            player.sendMessage("No usable chests found for $mat.")
            return
        }

        val affectedChests = ArrayList<Block>()
        val doubleChests = ArrayList<InventoryHolder?>()

        for (block in useableChests) {
            val inv = (block.state as Container).inventory
            if (inv.holder is DoubleChest) {
                val dc = inv.holder as DoubleChest?
                if (doubleChests.contains(dc?.leftSide)) continue
                doubleChests.add(dc?.leftSide)
            }
            if (InvUtils.searchItemInContainers(mat, inv, InvUnloadModule())) affectedChests.add(block)
        }

        InvUnloadModule().print(player)
        if (affectedChests.isEmpty()) player.sendMessage("No chests contain $mat.")

        for (block in affectedChests) InvUnloadModule().chestEffect(block, player)
        InvUnloadModule().play(player, affectedChests)
    }
}