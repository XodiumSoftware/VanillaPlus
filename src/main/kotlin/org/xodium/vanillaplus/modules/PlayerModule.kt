package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import io.papermc.paper.datacomponent.item.ResolvableProfile
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.bukkit.persistence.PersistentDataType
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.pdcs.PlayerPDC.nickname
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt

/** Represents a module handling player mechanics within the system. */
internal class PlayerModule(
    private val tabListModule: TabListModule,
) : ModuleInterface<PlayerModule.Config> {
    override val config: Config = Config()

    private val playerSkullXpKey = NamespacedKey(instance, "player_head_xp")
    private val playerSkullXpRecipeKey = NamespacedKey(instance, "player_head_xp_recipe")

    override fun enabled(): Boolean = config.enabled && tabListModule.enabled()

    override fun cmds(): List<CommandData> =
        listOf(
            CommandData(
                Commands
                    .literal("nickname")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .executes { ctx ->
                        ctx.tryCatch {
                            if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                            nickname(it.sender as Player, "")
                        }
                    }.then(
                        Commands
                            .argument("name", StringArgumentType.greedyString())
                            .executes { ctx ->
                                ctx.tryCatch {
                                    if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                                    nickname(it.sender as Player, StringArgumentType.getString(ctx, "name"))
                                }
                            },
                    ),
                "Allows players to set or remove their nickname",
                listOf("nick"),
            ),
        )

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance::class.simpleName}.nickname".lowercase(),
                "Allows use of the nickname command",
                PermissionDefault.TRUE,
            ),
        )

    init {
        if (enabled()) instance.server.addRecipe(recipe())
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return
        val player = event.player
        player.displayName(player.nickname()?.mm())

        if (config.i18n.playerJoinMsg.isEmpty()) return
        event.joinMessage(null)
        instance.server.onlinePlayers
            .filter { it.uniqueId != player.uniqueId }
            .forEach {
                it.sendMessage(
                    config.i18n.playerJoinMsg.mm(
                        Placeholder.component("player", player.displayName()),
                    ),
                )
            }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerQuitEvent) {
        if (!enabled() || config.i18n.playerQuitMsg.isEmpty()) return
        event.quitMessage(config.i18n.playerQuitMsg.mm(Placeholder.component("player", event.player.displayName())))
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: PlayerDeathEvent) {
        if (!enabled()) return
        val killer = event.entity.killer ?: return
        event.entity.world.dropItemNaturally(
            event.entity.location,
            playerSkull(event.entity, killer, event.droppedExp),
        )
        // TODO
//        if (config.i18n.playerDeathMsg.isNotEmpty()) event.deathMessage()
//        if (config.i18n.playerDeathScreenMsg.isNotEmpty()) event.deathScreenMessageOverride()
    }

    @EventHandler
    fun on(event: PlayerAdvancementDoneEvent) {
        if (!enabled() || config.i18n.playerAdvancementDoneMsg.isEmpty()) return
        event.message(
            config.i18n.playerAdvancementDoneMsg.mm(
                Placeholder.component("player", event.player.displayName()),
                Placeholder.component("advancement", event.advancement.displayName()),
            ),
        )
    }

    @EventHandler
    fun on(event: InventoryClickEvent) {
        if (!enabled()) return

        val player = event.whoClicked as? Player ?: return

        if (event.clickedInventory?.type != InventoryType.PLAYER) return

        if (event.click == config.enderChestClickType &&
            event.currentItem?.type == Material.ENDER_CHEST
        ) {
            event.isCancelled = true
            player.openInventory(player.enderChest)
        }

        val item = event.currentItem ?: return

        if (event.click == config.shulkerClickType &&
            Tag.SHULKER_BOXES.isTagged(item.type)
        ) {
            event.isCancelled = true

            val meta = item.itemMeta as? BlockStateMeta ?: return
            val shulker = meta.blockState as? ShulkerBox ?: return
            val inventory =
                player.server.createInventory(player, shulker.inventory.size, meta.displayName() ?: meta.itemName())

            inventory.contents = shulker.inventory.contents

            player.setMetadata("open_shulker", FixedMetadataValue(instance, item))
            player.openInventory(inventory)
        }
    }

    @EventHandler
    fun on(event: InventoryCloseEvent) {
        if (!enabled()) return

        val player = event.player as? Player ?: return
        val metaItem = player.getMetadata("open_shulker").firstOrNull()?.value() as? ItemStack ?: return

        player.removeMetadata("open_shulker", instance)

        val meta = metaItem.itemMeta as? BlockStateMeta ?: return
        val shulker = meta.blockState as? ShulkerBox ?: return

        shulker.inventory.contents = event.inventory.contents

        meta.blockState = shulker

        metaItem.itemMeta = meta
    }

    /**
     * Sets the nickname of the player to the given name.
     * @param player The player whose nickname is to be set.
     * @param name The new nickname for the player.
     */
    private fun nickname(
        player: Player,
        name: String,
    ) {
        player.nickname(name)
        player.displayName(player.nickname()?.mm())
        tabListModule.updatePlayerDisplayName(player)
        player.sendActionBar(config.i18n.nicknameUpdated.mm(Placeholder.component("nickname", player.displayName())))
    }

    /**
     * Creates a custom player skull item when a player is killed.
     * @param entity The player whose head is being created.
     * @param killer The player who killed the entity.
     * @param xp The amount of experience associated with the kill, stored in the skull.
     * @return An [ItemStack] representing the customized player head.
     */
    @Suppress("UnstableApiUsage")
    private fun playerSkull(
        entity: Player,
        killer: Player,
        xp: Int,
    ): ItemStack =
        ItemStack.of(Material.PLAYER_HEAD).apply {
            setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(entity.playerProfile))
            setData(
                DataComponentTypes.CUSTOM_NAME,
                config.i18n.playerHeadName.mm(Placeholder.component("player", entity.name.mm())),
            )
            setData(
                DataComponentTypes.LORE,
                ItemLore
                    .lore(
                        config.i18n.playerHeadLore
                            .mm(
                                Placeholder.component("player", entity.name.mm()),
                                Placeholder.component("killer", killer.name.mm()),
                                Placeholder.parsed("xp", xp.toString()),
                            ),
                    ),
            )
            editPersistentDataContainer { it.set(playerSkullXpKey, PersistentDataType.INTEGER, xp) }
        }

    /**
     * Creates a shapeless recipe for converting a player skull into an experience bottle.
     * @return A [Recipe] representing the custom shapeless crafting recipe.
     */
    private fun recipe(): Recipe =
        ShapelessRecipe(playerSkullXpRecipeKey, ItemStack.of(Material.EXPERIENCE_BOTTLE))
            .addIngredient(1, Material.GLASS_BOTTLE)
            .addIngredient(1, Material.PLAYER_HEAD)

    data class Config(
        override var enabled: Boolean = true,
        var enderChestClickType: ClickType = ClickType.SHIFT_RIGHT,
        var shulkerClickType: ClickType = ClickType.SHIFT_RIGHT,
        var i18n: I18n = I18n(),
    ) : ModuleInterface.Config {
        data class I18n(
            var playerHeadName: String = "<player>’s Skull",
            var playerHeadLore: List<String> = listOf("<player> killed by <killer>", "Stored XP: <xp>"),
//            var playerDeathMsg: String = "<killer> ${"⚔".mangoFmt(true)} <player>",
            var playerJoinMsg: String = "<green>➕<reset> ${"›".mangoFmt(true)} <player>",
            var playerQuitMsg: String = "<red>➖<reset> ${"›".mangoFmt(true)} <player>",
            var playerDeathMsg: String = "☠ ${"›".mangoFmt(true)}",
            var playerDeathScreenMsg: String = "☠",
            var playerAdvancementDoneMsg: String =
                "\uD83C\uDF89 ${
                    "›".mangoFmt(
                        true,
                    )
                } <player> ${"has made the advancement:".mangoFmt()} <advancement>".mangoFmt(),
            var nicknameUpdated: String = "Nickname has been updated to: <nickname>".fireFmt(),
        )
    }
}
