@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.xodium.vanillaplus.modules

import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.ElderGuardian
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Player
import org.bukkit.entity.Wither
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.menus.RuneMenu
import org.xodium.vanillaplus.pdcs.PlayerPDC.runeSlots
import org.xodium.vanillaplus.utils.CommandUtils.playerExecuted
import org.xodium.vanillaplus.utils.Utils.MM
import kotlin.random.Random

/** Represents a module handling rune mechanics within the system. */
internal object RuneModule : ModuleInterface {
    val RUNE_TYPE_KEY = NamespacedKey(instance, "rune_type")

    private val HEALTH_MODIFIER_KEY = NamespacedKey(instance, "rune_health_bonus")

    /** Represents the type of rune that can be slotted. */
    enum class RuneType { HEALTH, }

    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("rune")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .playerExecuted { player, _ -> RuneMenu.open(player) },
                "Opens the rune equipment menu",
                listOf("runes"),
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.rune".lowercase(),
                "Allows use of the rune command",
                PermissionDefault.TRUE,
            ),
        )

    @EventHandler
    fun on(event: EntityDeathEvent) {
        val entity = event.entity

        if (entity !is ElderGuardian && entity !is Wither && entity !is EnderDragon) return
        if (Random.nextDouble() < Config.gemDropChance) event.drops.add(createGemForType(RuneType.HEALTH))
    }

    @EventHandler
    fun on(event: PlayerJoinEvent) {
        applyRuneModifiers(event.player, event.player.runeSlots)
    }

    @EventHandler
    fun on(event: InventoryCloseEvent) {
        if (!RuneMenu.openViews.containsKey(event.view)) return
        val player = event.player as? Player ?: return
        val slots =
            (0 until 5).map { i ->
                val item = event.view.topInventory.getItem(i) ?: return@map ""
                if (isRune(item)) {
                    item.itemMeta?.persistentDataContainer?.getOrDefault(RUNE_TYPE_KEY, PersistentDataType.STRING, "")
                        ?: ""
                } else {
                    ""
                }
            }
        player.runeSlots = slots
        applyRuneModifiers(player, slots)
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        if (!RuneMenu.openViews.containsKey(event.view)) return

        val clickedInv = event.clickedInventory ?: return

        if (clickedInv == event.view.topInventory) {
            if (event.cursor.type != Material.AIR && !isRune(event.cursor)) event.isCancelled = true
        } else if (event.isShiftClick) {
            val item = event.currentItem ?: return

            if (item.type != Material.AIR && !isRune(item)) event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: InventoryDragEvent) {
        if (!RuneMenu.openViews.containsKey(event.view)) return
        if (event.rawSlots.any { it < 5 } && !isRune(event.oldCursor)) event.isCancelled = true
    }

    /** Creates an [ItemStack] representing the given [RuneType]. */
    fun createGemForType(type: RuneType): ItemStack {
        val item = ItemStack.of(Material.AMETHYST_SHARD)

        item.editMeta { meta ->
            meta.displayName(MM.deserialize("<!italic><gradient:#CB2D3E:#EF473A>Health Gem</gradient>"))
            meta.lore(
                listOf(
                    MM.deserialize(
                        "<!italic><gray>Grants +${Config.healthPerGem.toInt()} max health when slotted</gray>",
                    ),
                ),
            )
            meta.persistentDataContainer.set(RUNE_TYPE_KEY, PersistentDataType.STRING, type.name)
        }

        return item
    }

    /** Returns `true` if the given [ItemStack] is a rune gem. */
    internal fun isRune(item: ItemStack): Boolean = item.itemMeta?.persistentDataContainer?.has(RUNE_TYPE_KEY) == true

    private fun applyRuneModifiers(
        player: Player,
        slots: List<String>,
    ) {
        val healthAttr = player.getAttribute(Attribute.MAX_HEALTH) ?: return

        healthAttr.modifiers.filter { it.key == HEALTH_MODIFIER_KEY }.forEach { healthAttr.removeModifier(it) }

        val healthCount = slots.count { it == RuneType.HEALTH.name }

        if (healthCount > 0) {
            healthAttr.addModifier(
                AttributeModifier(
                    HEALTH_MODIFIER_KEY,
                    healthCount * Config.healthPerGem,
                    AttributeModifier.Operation.ADD_NUMBER,
                ),
            )
        }
    }

    /** Represents the config of the module. */
    object Config {
        var gemDropChance: Double = 0.10
        var healthPerGem: Double = 2.0
    }
}
