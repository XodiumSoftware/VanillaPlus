package org.xodium.vanillaplus.modules

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ManaManager
import org.xodium.vanillaplus.managers.ManaManager.Config.MAX_MANA
import org.xodium.vanillaplus.pdcs.ItemPDC.isManaPotion
import org.xodium.vanillaplus.pdcs.PlayerPDC.mana

/**
 * Handles custom potion consumption effects, primarily mana restoration.
 * When a player consumes a potion marked with [isManaPotion], their mana pool
 * is instantly refilled to maximum and the mana bar is displayed.
 */
internal object PotionModule : ModuleInterface {
    /**
     * Handles the consumption of custom potions.
     * If the consumed item is a mana potion ([isManaPotion]), refills the player's
     * mana to [MAX_MANA] and shows the mana bar.
     * @param event The [PlayerItemConsumeEvent] to process.
     */
    @EventHandler
    fun onPotionConsume(event: PlayerItemConsumeEvent) {
        val player = event.player
        val item = event.item

        if (!item.isManaPotion) return

        player.mana = MAX_MANA
        ManaManager.showManaBar(player)
    }
}