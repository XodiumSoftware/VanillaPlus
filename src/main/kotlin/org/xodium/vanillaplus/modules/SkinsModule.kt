/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.SkinData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry

class SkinsModule : ModuleInterface {
    override fun enabled(): Boolean = Config.SkinsModule.ENABLED

    private val skinKey: NamespacedKey = NamespacedKey(instance, "vp_skin")

    private val itemSkins = listOf(
        SkinData(EntityType.WITHER, 100, Material.WITHER_SPAWN_EGG),
        SkinData(EntityType.ELDER_GUARDIAN, 101, Material.ELDER_GUARDIAN_SPAWN_EGG),
        SkinData(EntityType.WARDEN, 102, Material.WARDEN_SPAWN_EGG),
        SkinData(EntityType.ENDER_DRAGON, 103, Material.ENDER_DRAGON_SPAWN_EGG)
    )

    private fun buildSkinItem(skinData: SkinData) = ItemBuilder.from(skinData.material)
        .name(Utils.mangoFormat("${skinData.entityName} Skin"))
        .lore(listOf("<dark_gray>▶ <gray>Click to toggle custom skin <dark_gray>◀").mm())
        .asGuiItem { player, _ -> toggleSkin(player, skinData) }

    private fun isUnlocked(player: Player, skinData: SkinData): Boolean =
        skinData.unlockedPlayers.contains(player.uniqueId)

    private fun validateItemIsNotNullOrAir(itemStack: ItemStack?, player: Player): Boolean {
        if (itemStack == null || itemStack.type == Material.AIR) {
            player.sendMessage("${VanillaPlus.PREFIX}<red>You must hold an item in your hotbar!".mm())
            return false
        }
        return true
    }

    private fun validateItemIsSword(itemStack: ItemStack, player: Player): Boolean {
        if (itemStack.type !in MaterialRegistry.SWORDS) {
            player.sendMessage("${VanillaPlus.PREFIX}<red>This skin can only be applied to swords!".mm())
            return false
        }
        return true
    }

    private fun validateItemMeta(itemMeta: ItemMeta?, player: Player): Boolean {
        if (itemMeta == null) {
            player.sendMessage("${VanillaPlus.PREFIX}<red>This item cannot hold custom model data!".mm())
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
        if (!isUnlocked(player, skinData)) {
            player.sendMessage("${VanillaPlus.PREFIX}<red>Locked! Defeat the <dark_red>${skinData.entityName} <red>to unlock this skin.".mm())
            return
        }
        val heldItem = player.inventory.getItem(player.inventory.heldItemSlot) ?: return
        val itemMeta = getValidHeldItemMeta(player) ?: return
        val container = itemMeta.persistentDataContainer
        if (itemMeta.hasCustomModelData() && itemMeta.customModelData == skinData.model && container.has(
                skinKey,
                PersistentDataType.INTEGER
            )
        ) {
            itemMeta.setCustomModelData(null)
            container.remove(skinKey)
            heldItem.itemMeta = itemMeta
            player.sendMessage("${VanillaPlus.PREFIX}<green>Custom skin removed from your item!".mm())
        } else {
            itemMeta.setCustomModelData(skinData.model)
            container.set(skinKey, PersistentDataType.INTEGER, skinData.model)
            heldItem.itemMeta = itemMeta
            player.sendMessage("${VanillaPlus.PREFIX}<green>Custom skin applied to your item!".mm())
        }
    }

    private fun validateSkin(item: ItemStack?, player: org.bukkit.entity.HumanEntity) {
        if (item == null || item.type == Material.AIR) return
        val meta: ItemMeta = item.itemMeta ?: return
        if (meta.persistentDataContainer.has(skinKey, PersistentDataType.INTEGER)) {
            val skinModel = meta.persistentDataContainer.get(skinKey, PersistentDataType.INTEGER)
            val skinData: SkinData? = itemSkins.find { it.model == skinModel }
            if (skinData != null) {
                if (!skinData.unlockedPlayers.contains(player.uniqueId)) {
                    meta.setCustomModelData(null)
                    meta.persistentDataContainer.remove(skinKey)
                    item.itemMeta = meta
                    player.sendMessage("${VanillaPlus.PREFIX}<red>You do not own this skin, so it has been removed!".mm())
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        itemSkins.find { it.entityType == event.entityType }?.let { skin ->
            skin.unlockedPlayers.add(killer.uniqueId)
            killer.sendMessage(
                "${VanillaPlus.PREFIX}<dark_aqua>Congratulations! You have defeated the ${skin.entityName} and unlocked custom skins!".mm()
            )
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerItemHeldEvent) {
        val player = event.player
        val item = player.inventory.getItem(event.newSlot)
        validateSkin(item, player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: InventoryClickEvent) {
        val player = event.whoClicked
        val item = event.currentItem
        validateSkin(item, player)
    }

    override fun gui(): Gui {
        return buildGui {
            title(Utils.birdflopFormat("Skins"))
            component {
                render {
                    itemSkins.forEachIndexed { index, skin -> it.setItem(index, buildSkinItem(skin)) }
                    (4..7).forEach { index -> it.setItem(index, Utils.fillerItem) }
                    it.setItem(8, Utils.backItem)
                }
            }
        }
    }
}