/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

@file:Suppress("UnstableApiUsage")

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
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


class SkinsModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.SkinsModule().enabled

    private val nspacedKey = NamespacedKey(instance, this::class.simpleName.toString())

    private fun buildSkinItem(lore: List<Component>, skinData: SkinData) = ItemBuilder.from(skinData.material)
        .name(Utils.mangoFormat("${skinData.entityName} Skin").mm())
        .lore(lore)
        .asGuiItem { player, _ -> toggleSkin(player, skinData) }

    private fun validateItemIsNotNullOrAir(itemStack: ItemStack?, audience: Audience): Boolean =
        if (itemStack == null || itemStack.type == Material.AIR) {
            audience.sendActionBar(
                Utils.firewatchFormat("You must hold an item in your hotbar!").mm()
            ); false
        } else true

    private fun validateItemIsSword(itemStack: ItemStack, audience: Audience): Boolean =
        if (itemStack.type !in Tag.ITEMS_SWORDS.values.toSet()) {
            audience.sendActionBar(
                Utils.firewatchFormat("This skin can only be applied to swords!").mm()
            ); false
        } else true

    private fun validateItemMeta(itemMeta: ItemMeta?, audience: Audience): Boolean =
        if (itemMeta == null) {
            audience.sendActionBar(
                Utils.firewatchFormat("This item cannot hold custom model data!").mm()
            ); false
        } else true

    private fun getValidHeldItemMeta(player: Player): ItemMeta? =
        player.inventory.getItem(player.inventory.heldItemSlot)
            ?.takeIf { validateItemIsNotNullOrAir(it, player) && validateItemIsSword(it, player) }
            ?.itemMeta
            ?.takeIf { validateItemMeta(it, player) }

    private fun toggleSkin(player: Player, skinData: SkinData) {
        if (!SkinData.hasUnlocked(player.uniqueId, skinData)) return
        val audience = Audience.audience(player)
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
            audience.sendActionBar(Utils.mangoFormat("Custom skin removed from your item!").mm())
        } else {
            container.set(nspacedKey, PersistentDataType.STRING, skinData.model)
            component.strings = listOf(skinData.model)
            itemMeta.setCustomModelDataComponent(component)
            heldItem.itemMeta = itemMeta
            audience.sendActionBar(Utils.mangoFormat("Custom skin applied to your item!").mm())
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
                        Audience.audience(player).sendActionBar(
                            Utils.firewatchFormat("You have not unlocked this skin yet, so it has been removed!").mm()
                        )
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
                killer.showTitle(
                    Title.title(
                        Utils.firewatchFormat("Congratulations!").mm(),
                        Utils.mangoFormat("You have defeated the ${skinData.entityName} and unlocked its skin!")
                            .mm()
                    )
                )
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerItemHeldEvent) = validateSkin(event.player.inventory.getItem(event.newSlot), event.player)

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: InventoryClickEvent) = validateSkin(event.currentItem, event.whoClicked as Player)

    fun gui(player: Player): Gui {
        return buildGui {
            spamPreventionDuration = ConfigData().guiAntiSpamDuration
            title(Utils.firewatchFormat("Skins").mm())
            statelessComponent {
                ConfigData.SkinsModule().skins.forEachIndexed { index, skin ->
                    val lore = if (SkinData.hasUnlocked(player.uniqueId, skin)) {
                        listOf("<dark_gray>▶ <gray>Click to toggle custom skin <dark_gray>◀").mm()
                    } else {
                        listOf(
                            "<dark_gray>✖ <gray>Requirements:",
                            "   <red>Defeat <gold>${skin.entityName}",
                        ).mm()
                    }
                    it.setItem(index, buildSkinItem(lore, skin))
                }
                (4..7).forEach { index -> it.setItem(index, Utils.fillerItem) }
                it.setItem(8, Utils.backItem)
            }
        }
    }
}