/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.managers

import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

/** Represents the chest access manager within the system. */
object ChestAccessManager {

    /**
     * Retrieves the set of denied blocks for a player.
     * @param player The player whose denied blocks to retrieve.
     * @param key The NamespacedKey for the denied blocks.
     * @return A mutable set of denied block locations as strings.
     */
    private fun getDeniedSet(player: Player, key: NamespacedKey): MutableSet<String> {
        return player.persistentDataContainer.get(key, PersistentDataType.LIST.strings())?.toMutableSet()
            ?: mutableSetOf()

    }

    /**
     * Sets the denied blocks for a player.
     * @param player The player whose denied blocks to set.
     * @param key The NamespacedKey for the denied blocks.
     * @param set The set of denied block locations as strings.
     */
    private fun setDeniedSet(player: Player, key: NamespacedKey, set: Set<String>) {
        if (set.isEmpty()) player.persistentDataContainer.remove(key)
        else player.persistentDataContainer.set(key, PersistentDataType.LIST.strings(), set.toList())
    }

    /**
     * Denies access to a block for a player.
     * @param player The player to deny access to.
     * @param key The NamespacedKey for the denied blocks.
     * @param block The block to deny access to.
     */
    fun deny(player: Player, key: NamespacedKey, block: Block) {
        val set = getDeniedSet(player, key)
        set.add(block.location.serialize().toString())
        setDeniedSet(player, key, set)
    }

    /**
     * Allows access to a block for a player.
     * @param player The player to allow access to.
     * @param key The NamespacedKey for the denied blocks.
     * @param block The block to allow access to.
     */
    fun allow(player: Player, key: NamespacedKey, block: Block) {
        val set = getDeniedSet(player, key)
        set.remove(block.location.serialize().toString())
        setDeniedSet(player, key, set)
    }

    /**
     * Checks if a player is allowed access to a block.
     * @param player The player to check.
     * @param key The NamespacedKey for the denied blocks.
     * @param block The block to check.
     * @return True if the player is allowed access, false otherwise.
     */
    fun isAllowed(player: Player, key: NamespacedKey, block: Block): Boolean {
        return !getDeniedSet(player, key).contains(block.location.serialize().toString())
    }
}