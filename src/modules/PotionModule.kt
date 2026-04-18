package org.xodium.vanillaplus.modules

import org.bukkit.NamespacedKey
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.AreaEffectCloud
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.AreaEffectCloudApplyEvent
import org.bukkit.event.entity.LingeringPotionSplashEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
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

        if (!event.item.isManaPotion) return

        player.mana = MAX_MANA
        ManaManager.showManaBar(player)
    }

    @EventHandler
    fun on(event: PotionSplashEvent) {
        if (!event.potion.item.isManaPotion) return

        event.affectedEntities.filterIsInstance<Player>().forEach {
            it.mana = MAX_MANA
            ManaManager.showManaBar(it)
        }
    }

    @EventHandler
    fun on(event: LingeringPotionSplashEvent) {
        if (!event.entity.item.isManaPotion) return

        event.areaEffectCloud.persistentDataContainer.set(
            NamespacedKey(instance, "mana_cloud"),
            PersistentDataType.BOOLEAN,
            true,
        )
    }

    @EventHandler
    fun on(event: AreaEffectCloudApplyEvent) {
        val key = NamespacedKey(instance, "mana_cloud")

        if (!event.entity.persistentDataContainer.has(key, PersistentDataType.BOOLEAN)) return

        event.affectedEntities.filterIsInstance<Player>().forEach {
            it.mana = MAX_MANA
            ManaManager.showManaBar(it)
        }
    }

    @EventHandler
    fun on(event: ProjectileHitEvent) {
        val arrow = event.entity as? AbstractArrow ?: return

        if (!arrow.itemStack.isManaPotion) return

        when (val hit = event.hitEntity) {
            is Player -> {
                hit.mana = MAX_MANA
                ManaManager.showManaBar(hit)
            }

            is AreaEffectCloud -> {
                hit.persistentDataContainer.set(
                    NamespacedKey(instance, "mana_cloud"),
                    PersistentDataType.BOOLEAN,
                    true,
                )
            }
        }
    }
}
