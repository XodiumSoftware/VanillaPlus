/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import dev.triumphteam.gui.paper.builder.item.ItemBuilder
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.Damageable
import org.xodium.vanillaplus.Perms
import org.xodium.vanillaplus.Utils
import org.xodium.vanillaplus.Utils.mm
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.ConfigData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.old.AutoToolNotifyEvent
import org.xodium.vanillaplus.registries.MaterialRegistry
import java.util.*
import java.util.function.ToIntFunction
import java.util.stream.Collectors

class AutoToolModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigData.AutoToolModule().enabled

    val toolMap: HashMap<Material?, Tool?> = HashMap<Material?, Tool?>()

    val useAxeAsWeapon: Boolean = ConfigData.AutoToolModule().useAxeAsSword

    val pickaxes: ArrayList<Material?> = ArrayList<Material?>()

    val axes: ArrayList<Material?> = ArrayList<Material?>()

    val hoes: ArrayList<Material?> = ArrayList<Material?>()

    val shovels: ArrayList<Material?> = ArrayList<Material?>()

    val swords: ArrayList<Material?> = ArrayList<Material?>()

    val allTools: ArrayList<Material?> = ArrayList<Material?>()

    val instaBreakableByHand: ArrayList<Material?> = ArrayList<Material?>()

    val leaves: EnumSet<Material?> = EnumSet.noneOf<Material?>(Material::class.java)

    val weapons: ArrayList<Material?> = ArrayList<Material?>()

    companion object {
        const val HOTBAR_SIZE = 9
        const val INVENTORY_SIZE = 36
    }

    enum class Tool {
        PICKAXE,
        SHOVEL,
        SHEARS,
        AXE,
        HOE,
        SWORD,
        NONE
    }

    @EventHandler
    fun on(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return
        val player = event.damager as Player
        if (!player.hasPermission(Perms.AutoTool.USE)) return
        TODO("check if autotool is enabled in db")
        val entity = event.getEntity()
        if (!(entity is Monster && ConfigData.AutoToolModule().useSwordOnHostileMobs)) return
        val playerInventory = player.inventory
        val bestRoscoe = getBestRoscoeFromInventory(entity.type, player, useAxeAsWeapon)
        if (bestRoscoe == null || bestRoscoe == playerInventory.itemInMainHand) return
        switchToBestRoscoe(player, bestRoscoe, ConfigData.AutoToolModule().favoriteSlot)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: BlockBreakEvent) {
        instance.server.scheduler.runTaskLater(instance, Runnable {
            instance.server.pluginManager.callEvent(AutoToolNotifyEvent(event.player, event.getBlock()))
        }, 1)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: AutoToolNotifyEvent) {
        onPlayerInteractWithBlock(
            PlayerInteractEvent(
                event.getPlayer(),
                Action.LEFT_CLICK_BLOCK,
                event.getPlayer().inventory.itemInMainHand,
                event.block,
                BlockFace.SELF,
                EquipmentSlot.HAND
            )
        )
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerInteractWithBlock(event: PlayerInteractEvent) {
        val playerSetting = main.getPlayerSetting(event.getPlayer()) // FIX
        if (playerSetting.getBtcache().valid
            && event.clickedBlock != null && event.clickedBlock!!
                .type == playerSetting.getBtcache().lastMat
        ) return
        val player = event.player
        if (!player.hasPermission(Perms.AutoTool.USE)) return
        if (!hasBestToolsEnabled(player)) return
        val block = event.clickedBlock
        if (block == null) return
        if (block.type == Material.AIR) return
        val playerInventory = player.inventory
        if (ConfigData.AutoToolModule().dontSwitchDuringBattle && isWeapon(playerInventory.itemInMainHand))
            return
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return
        val bestTool = getBestToolFromInventory(block.type, player, playerInventory.itemInMainHand)
        if (bestTool == null || bestTool == playerInventory.itemInMainHand) {
            playerSetting.getBtcache().validate(block.type)
            return
        }
        switchToBestTool(player, bestTool, block.type)
        playerSetting.getBtcache().validate(block.type)
    }

    private fun hasBestToolsEnabled(player: Player): Boolean = TODO()

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(event: PlayerQuitEvent): Unit = TODO("check database if enabled")

    init {
        Arrays.stream<Material?>(Material.entries.toTypedArray()).forEach { material: Material? ->
            if (material!!.name.endsWith("_LEAVES")) leaves.add(material)
        }

        for ((c, t) in listOf(
            MaterialRegistry.WEAPONS to weapons,
            MaterialRegistry.INSTA_BREAKABLE_BY_HAND to instaBreakableByHand,
            Tag.ITEMS_HOES.values.toSet() to hoes,
            Tag.ITEMS_PICKAXES.values.toSet() to pickaxes,
            Tag.ITEMS_AXES.values.toSet() to axes,
            Tag.ITEMS_SHOVELS.values.toSet() to shovels,
            Tag.ITEMS_SWORDS.values.toSet() to swords,
            MaterialRegistry.NETHERITE_TOOLS to allTools,
            MaterialRegistry.DEFAULT_MATERIALS to allTools
        )) t.addAll(c)

        tagToMap(Tag.MINEABLE_AXE, Tool.AXE)
        tagToMap(Tag.MINEABLE_HOE, Tool.HOE)
        tagToMap(Tag.MINEABLE_PICKAXE, Tool.PICKAXE)
        tagToMap(Tag.MINEABLE_SHOVEL, Tool.SHOVEL)

        // NONE SPECIFIC
        tagToMap(Tag.FLOWERS, Tool.NONE)

        // CUSTOM
//        addToMap(Material.GLOWSTONE, Tool.PICKAXE) // TODO: Prefer SilkTouch
    }

    private fun getFavoriteSlot(player: Player): Int {
        return if (ConfigData.AutoToolModule().favoriteSlot == -1) {
            player.inventory.heldItemSlot
        } else {
            ConfigData.AutoToolModule().favoriteSlot
        }
    }

    private fun switchToBestRoscoe(player: Player, bestRoscoe: ItemStack?, favoriteSlot: Int) {
        var bestRoscoe = bestRoscoe
        val playerInventory = player.inventory
        if (bestRoscoe == null) {
            val currentItem = playerInventory.itemInMainHand
            if (isDamageable(currentItem)) return
            bestRoscoe =
                getNonToolItemFromArray(
                    inventoryToArray(player),
                    currentItem,
                    Material.BEDROCK
                )
        }
        if (bestRoscoe == null) {
            freeSlot(favoriteSlot, playerInventory)
            return
        }
        val positionInInventory = getPositionInInventory(bestRoscoe, playerInventory)
        if (positionInInventory != -1) {
            moveToolToSlot(positionInInventory, favoriteSlot, playerInventory)
        } else {
            freeSlot(favoriteSlot, playerInventory)
        }
    }

    private fun switchToBestTool(player: Player, itemStack: ItemStack?, target: Material?) {
        var bestTool = itemStack
        val playerInventory = player.inventory
        if (bestTool == null) {
            val currentItem = playerInventory.itemInMainHand
            if (isDamageable(currentItem)) return
            bestTool = getNonToolItemFromArray(inventoryToArray(player), currentItem, target)
        }
        if (bestTool == null) {
            freeSlot(getFavoriteSlot(player), playerInventory)
            return
        }
        val positionInInventory = getPositionInInventory(bestTool, playerInventory)
        if (positionInInventory != -1) {
            moveToolToSlot(positionInInventory, getFavoriteSlot(player), playerInventory)
        } else {
            freeSlot(getFavoriteSlot(player), playerInventory)
        }
    }

    /**
     * Adds materials from the given tag to the tool map.
     * @param tag The material tag.
     * @param tool The tool type.
     */
    private fun tagToMap(tag: Tag<Material>, tool: Tool) = tagToMap(tag, tool, null)

    /**
     * Adds materials from the given tag to the tool map.
     * @param tag The material tag.
     * @param tool The tool type.
     * @param filter If not null, only materials whose name contains this filter will be added.
     */
    private fun tagToMap(tag: Tag<Material>, tool: Tool, filter: String?) {
        tag.values.forEach { material ->
            if (filter == null || material.name.contains(filter)) {
                addToMap(material, tool)
            }
        }
    }

    /**
     * Adds materials from the given material to the tool map.
     * @param material The material tag.
     * @param tool The tool type.
     */
    private fun addToMap(material: Material, tool: Tool) {
        toolMap[material] = tool
    }

    fun isWeapon(itemStack: ItemStack): Boolean = itemStack.type in weapons

    fun isToolOrRoscoe(itemStack: ItemStack): Boolean =
        allTools.contains(itemStack.type) || swords.contains(itemStack.type)

    fun getBestToolType(mat: Material): Tool = toolMap[mat] ?: Tool.NONE

    fun hasSilkTouch(itemStack: ItemStack?): Boolean = itemStack?.itemMeta?.hasEnchant(Enchantment.SILK_TOUCH) == true

    fun inventoryToArray(player: Player): Array<ItemStack?> =
        Array(INVENTORY_SIZE) { player.inventory.getItem(it) }

    fun profitsFromSilkTouch(material: Material): Boolean = MaterialRegistry.PROFITS_FROM_SILK_TOUCH.contains(material)

    fun profitsFromFortune(material: Material): Boolean = MaterialRegistry.PROFITS_FROM_FORTUNE.contains(material)

    fun isTool(tool: Tool, itemStack: ItemStack): Boolean {
        val material = itemStack.type
        return when (tool) {
            Tool.PICKAXE -> pickaxes.contains(material)
            Tool.AXE -> axes.contains(material)
            Tool.SHOVEL -> shovels.contains(material)
            Tool.HOE -> hoes.contains(material)
            Tool.SHEARS -> itemStack.type == Material.SHEARS
            Tool.NONE -> isDamageable(itemStack)
            Tool.SWORD -> swords.contains(material)
        }
    }

    fun getNonToolItemFromArray(items: Array<ItemStack?>, currentItem: ItemStack, target: Material?): ItemStack? {
        if (instaBreakableByHand.contains(target) && !hoes.contains(currentItem.type) ||
            !isToolOrRoscoe(currentItem)
        ) return currentItem
        for (item in items) if (isDamageable(item)) return item
        return null
    }

    fun getBestItemStackFromArray(
        tool: Tool,
        itemStacks: Array<ItemStack?>,
        trySilkTouch: Boolean,
        itemStack: ItemStack,
        material: Material
    ): ItemStack? {
        if (tool == Tool.NONE) {
            return getNonToolItemFromArray(itemStacks, itemStack, material)
        }
        var list: MutableList<ItemStack?> = ArrayList<ItemStack?>()
        for (itemStack in itemStacks) {
            // TODO: Check if durability is 1
            if (isTool(tool, itemStack!!)) {
                if (!trySilkTouch) list.add(itemStack) else if (hasSilkTouch(itemStack)) list.add(itemStack)
            }
        }
        if (list.isEmpty()) {
            return if (trySilkTouch) getBestItemStackFromArray(tool, itemStacks, false, itemStack, material) else null
        }
        list.sortWith(Comparator.comparingInt<ItemStack?>(ToIntFunction { itemStack: ItemStack ->
            Utils.getMultiplier(itemStack)
        }).reversed())
//        TODO: change to robust Material name
        if (material.name.endsWith("DIAMOND_ORE")) list = putIronPlusBeforeGoldPickaxes(list)
        return list[0]
    }

    private fun putIronPlusBeforeGoldPickaxes(list: MutableList<ItemStack?>?): MutableList<ItemStack?> {
        if (list == null || list.isEmpty()) return list!!
        let {
            if (it.isTool(Tool.PICKAXE, list[0]!!)) {
                val newList = list.stream().filter { itemStack: ItemStack? ->
                    when (itemStack!!.type) {
                        Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.GOLDEN_PICKAXE -> return@filter false
                        else -> return@filter true
                    }
                }.collect(Collectors.toList())
                if (!newList.isEmpty()) return newList
            }
        }
        return list
    }

    fun getBestRoscoeFromArray(
        itemStacks: Array<ItemStack?>,
        entityType: EntityType,
        useAxe: Boolean
    ): ItemStack? {
        val itemStackArrayList = ArrayList<ItemStack?>()
        // TODO: Check if durability is 1
        for (itemStack in itemStacks) if (isRoscoe(itemStack!!, useAxe)) itemStackArrayList.add(itemStack)
        if (itemStackArrayList.isEmpty()) return null
        itemStackArrayList.sortWith(Comparator { o1: ItemStack?, o2: ItemStack? ->
            if (Utils.getDamage(o1, entityType) < Utils.getDamage(o2, entityType)) 1 else -1
        })
        return itemStackArrayList[0]
    }

    private fun isRoscoe(itemStack: ItemStack, useAxe: Boolean): Boolean = when {
        useAxe -> swords.contains(itemStack.type) || axes.contains(itemStack.type)
        else -> swords.contains(itemStack.type)
    }

    fun getBestToolFromInventory(
        material: Material,
        player: Player,
        itemStack: ItemStack
    ): ItemStack? {
        val items = inventoryToArray(player)
        val tool: Tool?
        if (!Utils.isLeaves(material) && material != Material.COBWEB) {
            tool = getBestToolType(material)
        } else {
            tool = if (Utils.hasShears(player.inventory.storageContents)) {
                Tool.SHEARS
            } else if (Utils.hasHoe(player.inventory.storageContents)
                && material != Material.COBWEB
            ) {
                Tool.HOE
            } else if (((ConfigData.AutoToolModule().considerSwordsForCobwebs
                        && material == Material.COBWEB) || (material != Material.COBWEB
                        && ConfigData.AutoToolModule().considerSwordsForLeaves))
                && Utils.hasSword(
                    player.inventory.storageContents
                )
            ) {
                Tool.SWORD
            } else {
                Tool.NONE
            }
        }
        val bestStack = getBestItemStackFromArray(tool, items, profitsFromSilkTouch(material), itemStack, material)
        if (bestStack == null) return getNonToolItemFromArray(items, itemStack, material)
        return bestStack
    }

    fun getBestRoscoeFromInventory(
        entityType: EntityType,
        player: Player,
        useAxe: Boolean
    ): ItemStack? = getBestRoscoeFromArray(inventoryToArray(player), entityType, useAxe)

    fun getPositionInInventory(itemStack: ItemStack, playerInventory: PlayerInventory): Int {
        for (i in 0..<Objects.requireNonNull<PlayerInventory>(playerInventory, "Inventory must not be null").size) {
            val currentItem = playerInventory.getItem(i)
            if (currentItem == null) continue
            if (currentItem == Objects.requireNonNull<ItemStack?>(itemStack, "Item must not be null")) return i
        }
        return -1
    }

    fun moveToolToSlot(source: Int, dest: Int, playerInventory: PlayerInventory) {
        playerInventory.heldItemSlot = dest
        if (source == dest) return
        val sourceItem = playerInventory.getItem(source)
        val destItem = playerInventory.getItem(dest)
        if (source < HOTBAR_SIZE) {
            playerInventory.heldItemSlot = source
            return
        }
        if (destItem == null) {
            playerInventory.setItem(dest, sourceItem)
            playerInventory.setItem(source, null)
        } else {
            playerInventory.setItem(source, destItem)
            playerInventory.setItem(dest, sourceItem)
        }
    }

    fun isDamageable(itemStack: ItemStack?): Boolean =
        itemStack == null || !itemStack.hasItemMeta() || itemStack.itemMeta !is Damageable

    fun freeSlot(source: Int, playerInventory: PlayerInventory) {
        playerInventory.itemInMainHand

        if (isDamageable(playerInventory.itemInMainHand)) return
        val item = playerInventory.getItem(source)
        if (item == null) return
        if (isDamageable(item)) return

        playerInventory.setItem(source, null)
        playerInventory.addItem(item)

        if (playerInventory.getItem(source) == null) {
            playerInventory.heldItemSlot = source
            return
        }
        for (i in source..<INVENTORY_SIZE) {
            if (playerInventory.getItem(i) == null) {
                playerInventory.setItem(i, item)
                playerInventory.setItem(source, null)
                playerInventory.heldItemSlot = source
                return
            }
        }
        for (i in 0..<HOTBAR_SIZE) {
            if (playerInventory.getItem(i) == null || isDamageable(playerInventory.getItem(i))) {
                playerInventory.heldItemSlot = i
            }
        }
    }

    val guiItem = ItemBuilder.from(Material.MILK_BUCKET)
        .name(Utils.mangoFormat("AutoTool").mm())
        .lore(
            listOf(
                "<dark_gray>▶ <gray>Click to toggle <dark_gray>◀",
                "",
                "<dark_gray>✖ <dark_aqua>Auto switch to best tool",
                "   <gray>Shortcut: <gold>/autotool | /at"
            ).mm()
        )
        .asGuiItem { player, _ -> toggle(player) }

    fun toggle(player: Player) {
        TODO("toggle in database")
    }
}