/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.managers

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.suggestion.SuggestionProvider
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.VanillaPlus.Companion.PREFIX
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import java.util.concurrent.CompletableFuture

/** Manages access to chests for players, allowing them to deny or allow access to specific blocks. */
object ChestAccessManager {
    private val denyKey = NamespacedKey(instance, "denied_chest")

    @Suppress("UnstableApiUsage")
    private val playerSuggestionProvider = SuggestionProvider<CommandSourceStack> { ctx, builder ->
        val server = ctx.source.sender.server
        server.onlinePlayers.forEach { builder.suggest(it.name) }
        CompletableFuture.completedFuture(builder.build())
    }

    @Suppress("UnstableApiUsage")
    fun cmds(): Collection<LiteralArgumentBuilder<CommandSourceStack>>? {
        return listOf(
            Commands.literal("clearchestaccess")
                .requires { it.sender.hasPermission(Perms.Use.CLEAR_CHEST_ACCESS) }
                .then(
                    Commands.argument("player", StringArgumentType.word())
                        .suggests(playerSuggestionProvider)
                        .executes {
                            val playerName = StringArgumentType.getString(it, "player")
                            val player = it.source.sender.server.getPlayer(playerName)
                            if (player != null) {
                                clear(player)
                                it.source.sender.sendMessage(
                                    "${PREFIX}Cleared denied chests for ${player.displayName().mm()}".fireFmt().mm()
                                )
                                1
                            } else {
                                it.source.sender.sendMessage(
                                    "${PREFIX}Player $playerName not found".fireFmt().mm()
                                )
                                0
                            }
                        }
                ).executes {
                    val player = it.source.sender as Player
                    clear(player)
                    player.sendMessage("${PREFIX}Your denied chests have been cleared".fireFmt().mm())
                    1
                }
        )
    }

    /**
     * Retrieves the set of denied blocks for a player.
     * @param player The player whose denied blocks to retrieve.
     * @return A mutable set of denied block locations as strings.
     */
    private fun getDeniedSet(player: Player): MutableSet<String> {
        return player.persistentDataContainer.get(denyKey, PersistentDataType.STRING)?.split(";")
            ?.filter { it.isNotBlank() }?.toMutableSet() ?: mutableSetOf()
    }

    /**
     * Sets the denied blocks for a player.
     * @param player The player whose denied blocks to set.
     * @param set The set of denied block locations as strings.
     */
    private fun setDeniedSet(player: Player, set: Set<String>) {
        if (set.isEmpty()) player.persistentDataContainer.remove(denyKey)
        else player.persistentDataContainer.set(denyKey, PersistentDataType.STRING, set.joinToString(";"))
    }

    /**
     * Denies access to a block for a player.
     * @param player The player to deny access to.
     * @param block The block to deny access to.
     */
    fun deny(player: Player, block: Block) {
        val set = getDeniedSet(player)
        set.add(block.location.serialize().toString())
        setDeniedSet(player, set)
    }

    /**
     * Allows access to a block for a player.
     * @param player The player to allow access to.
     * @param block The block to allow access to.
     */
    fun allow(player: Player, block: Block) {
        val set = getDeniedSet(player)
        set.remove(block.location.serialize().toString())
        setDeniedSet(player, set)
    }

    /**
     * Checks if a player is allowed access to a block.
     * @param player The player to check.
     * @param block The block to check.
     * @return True if the player is allowed access, false otherwise.
     */
    fun isAllowed(player: Player, block: Block): Boolean {
        return !getDeniedSet(player).contains(block.location.serialize().toString())
    }

    /**
     * Clears the denied blocks for a player.
     * @param player The player whose denied blocks to clear.
     */
    private fun clear(player: Player): Unit = setDeniedSet(player, emptySet())
}