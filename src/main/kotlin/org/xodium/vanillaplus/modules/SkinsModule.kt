/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.Database
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.data.SkinData
import org.xodium.vanillaplus.data.SkinData.Companion.getByEntityType
import org.xodium.vanillaplus.data.SkinData.Companion.getByModel
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry

class SkinsModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.SkinsModule().enabled

    private val nspacedKey = NamespacedKey(instance, this::class.simpleName.toString())

    private fun buildSkinItem(skinData: SkinData) = ItemBuilder.from(skinData.material)
        .name(Utils.mangoFormat("${skinData.entityName} Skin").mm())
        .lore(listOf("<dark_gray>▶ <gray>Click to toggle custom skin <dark_gray>◀").mm())
        .asGuiItem { player, _ -> toggleSkin(player, skinData) }

    private fun validateItemIsNotNullOrAir(itemStack: ItemStack?, player: Player): Boolean {
        if (itemStack == null || itemStack.type == Material.AIR) {
            player.showTitle(Utils.subtitle(Utils.firewatchFormat("You must hold an item in your hotbar!")))
            return false
        }
        return true
    }

    private fun validateItemIsSword(itemStack: ItemStack, player: Player): Boolean {
        if (itemStack.type !in MaterialRegistry.SWORDS) {
            player.showTitle(Utils.subtitle(Utils.firewatchFormat("This skin can only be applied to swords!")))
            return false
        }
        return true
    }

    private fun validateItemMeta(itemMeta: ItemMeta?, player: Player): Boolean {
        if (itemMeta == null) {
            player.showTitle(Utils.subtitle(Utils.firewatchFormat("This item cannot hold custom model data!")))
            return false
        }
        return true
    }

    private fun getValidHeldItemMeta(player: Player): ItemMeta? {
        val heldItem = player.inventory.getItem(player.inventory.heldItemSlot)
        if (!validateItemIsNotNullOrAir(heldItem, player)) return null
        if (!validateItemIsSword(heldItem!!, player)) return null
        val meta = heldItem.itemMeta
        if (!validateItemMeta(meta, player)) return null
        return meta
    }

    private fun toggleSkin(player: Player, skinData: SkinData) {
        if (!SkinData.hasUnlocked(player.uniqueId, skinData)) {
            player.showTitle(Utils.subtitle(Utils.firewatchFormat("Locked! Defeat the ${skinData.entityName} to unlock this skin")))
            return
        }
        val heldItem = player.inventory.getItem(player.inventory.heldItemSlot) ?: return
        val itemMeta = getValidHeldItemMeta(player) ?: return
        val container = itemMeta.persistentDataContainer
        val component = itemMeta.customModelDataComponent
        if (container.has(nspacedKey, PersistentDataType.STRING) &&
            container.get(nspacedKey, PersistentDataType.STRING) == skinData.model
        ) {
            container.remove(nspacedKey)
            itemMeta.setCustomModelDataComponent(null)
            heldItem.itemMeta = itemMeta
            player.showTitle(Utils.subtitle(Utils.mangoFormat("Custom skin removed from your item!")))
        } else {
            container.set(nspacedKey, PersistentDataType.STRING, skinData.model)
            component.strings = listOf(skinData.model)
            itemMeta.setCustomModelDataComponent(component)
            heldItem.itemMeta = itemMeta
            player.showTitle(Utils.subtitle(Utils.mangoFormat("Custom skin applied to your item!")))
        }
    }


    private fun validateSkin(item: ItemStack?, player: Player) {
        if (item == null || item.type == Material.AIR) return
        val meta = item.itemMeta ?: return
        if (meta.persistentDataContainer.has(nspacedKey, PersistentDataType.STRING)) {
            meta.persistentDataContainer.get(nspacedKey, PersistentDataType.STRING)?.let { skinModel ->
                ConfigData.SkinsModule().skins.getByModel(skinModel)?.let { skinData ->
                    if (!SkinData.hasUnlocked(player.uniqueId, skinData)) {
                        meta.setCustomModelDataComponent(null)
                        meta.persistentDataContainer.remove(nspacedKey)
                        item.itemMeta = meta
                        player.showTitle(Utils.subtitle(Utils.firewatchFormat("You have not unlocked this skin yet, so it has been removed!")))
                    }
                }
            }
        }
    }

    init {
        Database.createTable(this::class)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        ConfigData.SkinsModule().skins.getByEntityType(event.entityType)?.let { skinData ->
            if (!SkinData.hasUnlocked(killer.uniqueId, skinData)) {
                SkinData.setUnlocked(killer.uniqueId, skinData)
                killer.playSound(ConfigData.SkinsModule().soundUnlockSkin, Sound.Emitter.self())
                killer.showTitle(Utils.subtitle(Utils.mangoFormat("Congratulations! You have defeated the ${skinData.entityName} and unlocked its skin!")))
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerItemHeldEvent) = validateSkin(event.player.inventory.getItem(event.newSlot), event.player)

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: InventoryClickEvent) = validateSkin(event.currentItem, event.whoClicked as Player)

    override fun gui(): Gui {
        return buildGui {
            title(Utils.firewatchFormat("Skins").mm())
            component {
                render {
                    ConfigData.SkinsModule().skins.forEachIndexed { index, skin ->
                        it.setItem(
                            index,
                            buildSkinItem(skin)
                        )
                    }
                    (4..7).forEach { index -> it.setItem(index, Utils.fillerItem) }
                    it.setItem(8, Utils.backItem)
                }
            }
        }
    }
}