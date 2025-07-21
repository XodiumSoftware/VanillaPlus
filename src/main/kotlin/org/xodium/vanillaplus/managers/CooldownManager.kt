package org.xodium.vanillaplus.managers

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

/** Represents the cooldown manager within the system. */
internal object CooldownManager {
    /**
     * Gets the cooldown timestamp for a player and a specific cooldown key.
     * @param player The player to get the cooldown for.
     * @param key The NamespacedKey for the cooldown.
     * @return The timestamp, or `0L` if not set.
     */
    private fun getCooldown(player: Player, key: NamespacedKey): Long {
        return player.persistentDataContainer.get(key, PersistentDataType.LONG) ?: 0L
    }

    /**
     * Sets the cooldown timestamp for a player and a specific cooldown key.
     * @param player The player to set the cooldown for.
     * @param key The NamespacedKey for the cooldown.
     * @param timestamp The timestamp to set (usually System.currentTimeMillis()).
     */
    fun setCooldown(player: Player, key: NamespacedKey, timestamp: Long) {
        player.persistentDataContainer.set(key, PersistentDataType.LONG, timestamp)
    }

    /**
     * Checks if a player is still on cooldown for a specific key.
     * @param player The player to check.
     * @param key The NamespacedKey for the cooldown.
     * @param cooldownDuration The cooldown duration in milliseconds.
     * @return True if the cooldown is active, false otherwise.
     */
    fun isOnCooldown(player: Player, key: NamespacedKey, cooldownDuration: Long): Boolean {
        return System.currentTimeMillis() < getCooldown(player, key) + cooldownDuration
    }
}