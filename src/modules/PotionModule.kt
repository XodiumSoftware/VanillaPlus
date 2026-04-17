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
    @EventHandler
    fun on(event: PlayerItemConsumeEvent) {
        val player = event.player
        val item = event.item

        if (!item.isManaPotion) return

        player.mana = MAX_MANA
        ManaManager.showManaBar(player)
    }

    // TODO: impl events for splash/lingering potions and tipped arrow.
}
