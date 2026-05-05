package org.xodium.illyriaplus.managers

import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.xodium.illyriaplus.Utils.MM
import org.xodium.illyriaplus.managers.XpManager.NO_XP_SOUND

/**
 * Manages XP cost validation and deduction for spell enchantments.
 * Provides a unified way to handle spell casting costs across all wand-based spells.
 */
internal object XpManager {
    private const val NO_XP_MSG = "<gradient:#CB2D3E:#EF473A><b>Not enough XP!</b></gradient>"

    private val NO_XP_SOUND: Sound = Sound.sound(Key.key("block.beacon.deactivate"), Sound.Source.PLAYER, 1.0f, 1.0f)
    private val ACTIVE_GAME_MODES = setOf(GameMode.SURVIVAL, GameMode.ADVENTURE)

    /**
     * Validates a spell cast interaction and consumes XP if all checks pass.
     *
     * Checks:
     * - Action is left-click (air or block)
     * - Held item is a Blaze Rod with the required enchantment
     * - Player is in an appropriate game mode
     * - Player has sufficient XP (for survival/adventure)
     *
     * On success (creative mode): cancels event, returns player (no XP cost)
     * On success (survival/adventure): cancels event, deducts XP, returns player
     * On failure (insufficient XP): plays [NO_XP_SOUND], sends action bar message, returns null
     *
     * @param event The [PlayerInteractEvent] to validate
     * @param enchantment The [Enchantment] that must be present on the held item
     * @param xpCost The amount of XP points required and consumed on success
     * @return The [Player] if all checks pass, `null` otherwise
     */
    fun consumeXp(
        event: PlayerInteractEvent,
        enchantment: Enchantment,
        xpCost: Int,
    ): Player? {
        if (event.action != Action.LEFT_CLICK_AIR && event.action != Action.LEFT_CLICK_BLOCK) return null

        val item = event.item ?: return null

        if (item.type != Material.BLAZE_ROD) return null
        if (!item.containsEnchantment(enchantment)) return null

        val player = event.player

        if (player.gameMode == GameMode.CREATIVE) {
            event.isCancelled = true
            return player
        }

        if (player.gameMode !in ACTIVE_GAME_MODES) return null

        if (!player.hasEnoughXp(xpCost)) {
            player.playSound(NO_XP_SOUND)
            player.sendActionBar(MM.deserialize(NO_XP_MSG))
            return null
        }

        event.isCancelled = true
        player.giveExp(-xpCost)

        return player
    }

    /**
     * Checks if the player has at least [xpCost] experience points.
     * Uses total experience points (levels * level cost + progress)
     * rather than raw levels for finer granularity.
     *
     * @param xpCost The XP point threshold to check
     * @return `true` if the player has enough XP, `false` otherwise
     */
    private fun Player.hasEnoughXp(xpCost: Int): Boolean = calculateTotalExperiencePoints() >= xpCost
}
