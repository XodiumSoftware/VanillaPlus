/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.Gui
import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import dev.triumphteam.gui.paper.kotlin.builder.buildGui
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDeathEvent
import org.xodium.vanillaplus.Config
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.registries.MaterialRegistry
import java.util.*

class SkinsModule : ModuleInterface {
    override fun enabled(): Boolean = Config.SkinsModule.ENABLED

    private val witherWinners: MutableSet<UUID> = mutableSetOf()
    private val elderGuardianWinners: MutableSet<UUID> = mutableSetOf()
    private val enderDragonWinners: MutableSet<UUID> = mutableSetOf()
    private val wardenWinners: MutableSet<UUID> = mutableSetOf()

    private val witherSkinData = 100
    private val elderGuardianSkinData = 101
    private val enderDragonSkinData = 102
    private val wardenSkinData = 103

    private val witherSkin = ItemBuilder.from(Material.WITHER_SKELETON_SKULL)
        .name(Utils.mangoFormat("Wither Boss Skin"))
        .lore(listOf("<dark_gray>▶ <gray>Click the item to toggle custom skin <dark_gray>◀").mm())
        .asGuiItem { player, _ -> toggleSkin(player, witherSkinData) }
    private val elderGuardianSkin = ItemBuilder.from(Material.ELDER_GUARDIAN_SPAWN_EGG)
        .name(Utils.mangoFormat("Elder Guardian Skin"))
        .lore(listOf("<dark_gray>▶ <gray>Click the item to toggle custom skin <dark_gray>◀").mm())
        .asGuiItem { player, _ -> toggleSkin(player, elderGuardianSkinData) }
    private val enderDragonSkin = ItemBuilder.from(Material.DRAGON_HEAD)
        .name(Utils.mangoFormat("Ender Dragon Skin"))
        .lore(listOf("<dark_gray>▶ <gray>Click the item to toggle custom skin <dark_gray>◀").mm())
        .asGuiItem { player, _ -> toggleSkin(player, enderDragonSkinData) }
    private val wardenSkin = ItemBuilder.from(Material.WARDEN_SPAWN_EGG)
        .name(Utils.mangoFormat("Warden Skin"))
        .lore(listOf("<dark_gray>▶ <gray>Click the item to toggle custom skin <dark_gray>◀").mm())
        .asGuiItem { player, _ -> toggleSkin(player, wardenSkinData) }

    private fun toggleSkin(player: Player, skinData: Int) {
        if (!when (skinData) {
                witherSkinData -> witherWinners.contains(player.uniqueId)
                elderGuardianSkinData -> elderGuardianWinners.contains(player.uniqueId)
                enderDragonSkinData -> enderDragonWinners.contains(player.uniqueId)
                wardenSkinData -> wardenWinners.contains(player.uniqueId)
                else -> false
            }
        ) {
            player.sendMessage(VanillaPlus.PREFIX + "<red>You haven't unlocked this custom skin yet!".mm())
            return
        }
        val heldItem = player.inventory.getItem(player.inventory.heldItemSlot)
        if (heldItem == null || heldItem.type == Material.AIR) {
            player.sendMessage(VanillaPlus.PREFIX + "<red>You must hold an item in your hotbar!".mm())
            return
        }
        if (heldItem.type !in MaterialRegistry.SWORDS) {
            player.sendMessage(VanillaPlus.PREFIX + "<red>This skin can only be applied to swords!".mm())
            return
        }
        val meta = heldItem.itemMeta ?: run {
            player.sendMessage(VanillaPlus.PREFIX + "<red>This item cannot hold custom model data!")
            return
        }
        if (meta.hasCustomModelData() && meta.customModelData == skinData) {
            meta.setCustomModelData(null)
            heldItem.itemMeta = meta
            player.sendMessage(VanillaPlus.PREFIX + "<green>Custom skin removed from your item!".mm())
        } else {
            meta.setCustomModelData(skinData)
            heldItem.itemMeta = meta
            player.sendMessage(VanillaPlus.PREFIX + "<green>Custom skin applied to your item!".mm())
        }
    }

    private val bossMap: Map<EntityType, Pair<MutableSet<UUID>, String>> = mapOf(
        EntityType.WITHER to (witherWinners to "Wither"),
        EntityType.ELDER_GUARDIAN to (elderGuardianWinners to "Elder Guardian"),
        EntityType.ENDER_DRAGON to (enderDragonWinners to "Ender Dragon"),
        EntityType.WARDEN to (wardenWinners to "Warden")
    )

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        val bossData = bossMap[event.entityType] ?: return
        bossData.first.add(killer.uniqueId)
        killer.sendMessage(
            VanillaPlus.PREFIX +
                    "<dark_aqua>Congratulations! You have defeated the ${bossData.second} and unlocked custom skins!".mm()
        )
    }

    fun Gui(): Gui {
        return buildGui {
            spamPreventionDuration = Utils.antiSpamDuration
            title(Utils.birdflopFormat("Skins"))
            statelessComponent { gui ->
                gui.setItem(0, witherSkin)
                gui.setItem(1, elderGuardianSkin)
                gui.setItem(2, enderDragonSkin)
                gui.setItem(3, wardenSkin)
                (4..7).forEach { index -> gui.setItem(index, Utils.fillerItem) }
                gui.setItem(8, Utils.backItem)
            }
        }
    }
}