/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package de.jeff_media.bestTools

import de.jeff_media.bestTools.LeavesUtils.hasHoe
import de.jeff_media.bestTools.LeavesUtils.hasShears
import de.jeff_media.bestTools.LeavesUtils.hasSword
import de.jeff_media.bestTools.LeavesUtils.isLeaves
import de.jeff_media.bestTools.SwordUtils.getDamage
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import java.util.*
import java.util.function.ToIntFunction
import java.util.stream.Collectors

/**
 * This will probably be a separate plugin called BestTool or something
 */
class AutoToolsHandler internal constructor(main: Main?) {
    val main: Main = Objects.requireNonNull<Main>(main, "Main must not be null")

    @JvmField
    val toolMap: HashMap<Material?, Tool?> = HashMap<Material?, Tool?>()

    @JvmField
    val globalBlacklist: HashSet<Material?> = HashSet<Material?>()

    // TODO: Cache valid tool materials here
    @JvmField
    val pickaxes: ArrayList<Material?> = ArrayList<Material?>()

    @JvmField
    val axes: ArrayList<Material?> = ArrayList<Material?>()

    @JvmField
    val hoes: ArrayList<Material?> = ArrayList<Material?>()

    @JvmField
    val shovels: ArrayList<Material?> = ArrayList<Material?>()

    @JvmField
    val swords: ArrayList<Material?> = ArrayList<Material?>()

    @JvmField
    val allTools: ArrayList<Material?> = ArrayList<Material?>()

    @JvmField
    val instaBreakableByHand: ArrayList<Material?> = ArrayList<Material?>()

    val leaves: EnumSet<Material?> = EnumSet.noneOf<Material?>(Material::class.java)


    @JvmField
    val weapons: ArrayList<Material?> = ArrayList<Material?>()

    init {
        for (name in main!!.config.getStringList("global-block-blacklist")) {
            val mat = Material.valueOf(name.uppercase(Locale.getDefault()))
            main.debug("Adding to global block blacklist: " + mat.name)
            globalBlacklist.add(mat)
        }

        Arrays.stream<Material?>(Material.entries.toTypedArray()).forEach { material: Material? ->
            if (material!!.name.endsWith("_LEAVES")) {
                leaves.add(material)
            }
        }
    }

    fun isWeapon(itemStack: ItemStack): Boolean = itemStack.type in weapons

    fun isToolOrRoscoe(itemStack: ItemStack): Boolean =
        allTools.contains(itemStack.type) || swords.contains(itemStack.type)

    fun getBestToolType(mat: Material): Tool {
        var bestTool = toolMap[mat]
        if (bestTool == null) bestTool = Tool.NONE
        main.debug("Best ToolType for " + mat + " is " + bestTool.name)
        return bestTool
    }

    fun profitsFromSilkTouch(material: Material): Boolean {
        when (material) {
            Material.GLOWSTONE,
            Material.ENDER_CHEST,
            Material.QUARTZ,
            Material.SPAWNER,
            Material.SEA_LANTERN,
            Material.NETHER_GOLD_ORE,
            Material.GLASS,
            Material.BLACK_STAINED_GLASS,
            Material.BLUE_STAINED_GLASS,
            Material.BROWN_STAINED_GLASS,
            Material.CYAN_STAINED_GLASS,
            Material.GRAY_STAINED_GLASS,
            Material.GREEN_STAINED_GLASS,
            Material.LIGHT_BLUE_STAINED_GLASS,
            Material.LIGHT_GRAY_STAINED_GLASS,
            Material.LIME_STAINED_GLASS,
            Material.MAGENTA_STAINED_GLASS,
            Material.ORANGE_STAINED_GLASS,
            Material.PINK_STAINED_GLASS,
            Material.PURPLE_STAINED_GLASS,
            Material.RED_STAINED_GLASS,
            Material.WHITE_STAINED_GLASS,
            Material.YELLOW_STAINED_GLASS -> return true

            else -> return false
        }
    }

    // TODO: Implement profitsFromFortune()
    fun isTool(tool: Tool, itemStack: ItemStack): Boolean {
        val m = itemStack.type
        return when (tool) {
            Tool.PICKAXE -> pickaxes.contains(m)
            Tool.AXE -> axes.contains(m)
            Tool.SHOVEL -> shovels.contains(m)
            Tool.HOE -> hoes.contains(m)
            Tool.SHEARS -> itemStack.type == Material.SHEARS
            Tool.NONE -> isDamageable(itemStack)
            Tool.SWORD -> swords.contains(m)
        }
    }

    fun getNonToolItemFromArray(items: Array<ItemStack?>, currentItem: ItemStack, target: Material?): ItemStack? {
        if (instaBreakableByHand.contains(target) && !hoes.contains(currentItem.type) ||
            !isToolOrRoscoe(currentItem)
        ) return currentItem

        for (item in items) {
            if (isDamageable(item)) {
                return item
            }
        }
        return null
    }

    fun hasSilkTouch(itemStack: ItemStack?): Boolean {
        if (itemStack == null) return false
        if (!itemStack.hasItemMeta()) return false
        return Objects.requireNonNull<ItemMeta?>(itemStack.itemMeta).hasEnchant(Enchantment.SILK_TOUCH)
    }

    fun getBestItemStackFromArray(
        tool: Tool,
        itemStacks: Array<ItemStack?>,
        trySilkTouch: Boolean,
        itemStack: ItemStack,
        material: Material
    ): ItemStack? {
        if (tool == Tool.NONE) {
            main.debug("getNonToolItemFromArray")
            return getNonToolItemFromArray(itemStacks, itemStack, material)
        }
        var list: MutableList<ItemStack?> = ArrayList<ItemStack?>()
        for (itemStack in itemStacks) {
            // TODO: Check if durability is 1
            if (isTool(tool, itemStack!!)) {
                if (!trySilkTouch) {
                    list.add(itemStack)
                } else {
                    if (hasSilkTouch(itemStack)) {
                        list.add(itemStack)
                    }
                }
            }
        }
        if (list.isEmpty()) {
            return if (trySilkTouch) {
                getBestItemStackFromArray(tool, itemStacks, false, itemStack, material)
            } else {
                null
            }
        }
        list.sort(Comparator.comparingInt<ItemStack?>(ToIntFunction { itemStack: ItemStack ->
            EnchantmentUtils.getMultiplier(itemStack)
        }).reversed())
//        TODO: change to robust Material name
        if (material.name.endsWith("DIAMOND_ORE")) {
            list = putIronPlusBeforeGoldPickaxes(list)
        }
        return list[0]
    }

    private fun putIronPlusBeforeGoldPickaxes(list: MutableList<ItemStack?>?): MutableList<ItemStack?> {
        if (list == null || list.isEmpty()) return list!!
        main.toolHandler?.let {
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
        itemStackArrayList.sort(Comparator { o1: ItemStack?, o2: ItemStack? ->
            if (getDamage(o1, entityType) < getDamage(
                    o2,
                    entityType
                )
            ) 1 else -1
        })
        return itemStackArrayList[0]
    }

    private fun isRoscoe(itemStack: ItemStack, useAxe: Boolean): Boolean = when {
        useAxe -> swords.contains(itemStack.type) || axes.contains(itemStack.type)
        else -> swords.contains(itemStack.type)
    }

    fun inventoryToArray(player: Player, hotBarOnly: Boolean): Array<ItemStack?> {
        val items = arrayOfNulls<ItemStack>((if (hotBarOnly) HOTBAR_SIZE else INVENTORY_SIZE))
        for (i in 0..<(if (hotBarOnly) HOTBAR_SIZE else INVENTORY_SIZE)) items[i] = player.inventory.getItem(i)
        return items
    }

    fun getBestToolFromInventory(
        material: Material,
        player: Player,
        hotBarOnly: Boolean,
        itemStack: ItemStack
    ): ItemStack? {
        val items = inventoryToArray(player, hotBarOnly)

        val tool: Tool?
        if (!isLeaves(material) && material != Material.COBWEB) {
            tool = getBestToolType(material)
        } else {
            tool = if (hasShears(hotBarOnly, player.inventory.storageContents)) {
                Tool.SHEARS
            } else if (hasHoe(hotBarOnly, player.inventory.storageContents) && material != Material.COBWEB) {
                Tool.HOE
            } else if (((main.config
                    .getBoolean("consider-swords-for-cobwebs") && material == Material.COBWEB) || (material != Material.COBWEB && main.config
                    .getBoolean("consider-swords-for-leaves"))) && hasSword(
                    hotBarOnly,
                    player.inventory.storageContents
                )
            ) {
                Tool.SWORD
            } else {
                Tool.NONE
            }
        }
        val bestStack = getBestItemStackFromArray(tool, items, profitsFromSilkTouch(material), itemStack, material)
        if (bestStack == null) {
            main.debug("bestStack is null")
            return getNonToolItemFromArray(items, itemStack, material)
        }
        main.debug("bestStack is $bestStack")
        return bestStack
    }

    fun getBestRoscoeFromInventory(
        entityType: EntityType,
        player: Player,
        hotBarOnly: Boolean,
        itemStack: ItemStack?,
        useAxe: Boolean
    ): ItemStack? = getBestRoscoeFromArray(inventoryToArray(player, hotBarOnly), itemStack, entityType, useAxe)

    fun getPositionInInventory(itemStack: ItemStack, playerInventory: PlayerInventory): Int {
        for (i in 0..<Objects.requireNonNull<PlayerInventory>(playerInventory, "Inventory must not be null").size) {
            val currentItem = playerInventory.getItem(i)
            if (currentItem == null) continue
            if (currentItem == Objects.requireNonNull<ItemStack?>(itemStack, "Item must not be null")) {
                main.debug(String.format("Found perfect tool %s at slot %d", currentItem.type.name, i))
                return i
            }
        }
        return -1
    }

    fun moveToolToSlot(source: Int, dest: Int, playerInventory: PlayerInventory) {
        main.debug(String.format("Moving item from slot %d to %d", source, dest))
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

    fun isDamageable(itemStack: ItemStack?): Boolean {
        if (itemStack == null) return true
        if (!itemStack.hasItemMeta()) return true
        val itemMeta = itemStack.itemMeta
        if (itemMeta is Damageable) {
            main.debug(itemStack.type.name + " is damageable")
            return false
        } else {
            main.debug(itemStack.type.name + " is NOT damageable")
            return true
        }
    }

    fun freeSlot(source: Int, playerInventory: PlayerInventory) {
        playerInventory.itemInMainHand

        if (isDamageable(playerInventory.itemInMainHand)) return

        val item = playerInventory.getItem(source)

        if (item == null) return

        if (isDamageable(item)) return

        main.debug(String.format("Trying to free slot %d", source))

        playerInventory.setItem(source, null)
        playerInventory.addItem(item)

        if (playerInventory.getItem(source) == null) {
            main.debug("Freed slot")
            playerInventory.heldItemSlot = source
            return
        }
        main.debug("Could not free slot yet...")
        for (i in source..<INVENTORY_SIZE) {
            if (playerInventory.getItem(i) == null) {
                playerInventory.setItem(i, item)
                playerInventory.setItem(source, null)
                playerInventory.heldItemSlot = source
                main.debug("Freed slot on second try")
                return
            }
        }

        main.debug("WARNING: COULD NOT FREE SLOT AT ALL")

        for (i in 0..<HOTBAR_SIZE) {
            if (playerInventory.getItem(i) == null || isDamageable(playerInventory.getItem(i))) {
                main.debug("Found not damageable item at slot $i")
                playerInventory.heldItemSlot = i
            }
        }
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

    companion object {
        const val HOTBAR_SIZE: Int = 9
        const val INVENTORY_SIZE: Int = 36
    }
}
